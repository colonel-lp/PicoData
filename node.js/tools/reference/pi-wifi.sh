#!/bin/sh
# Historical USB-uplink helper recovered from the 2026-10-30 chat.
# NOT an installer or a replacement for the current STA-only setup.
# Private profile/SSID values have been replaced with conspicuous placeholders.
set -eu
export LC_ALL=C
PATH=/usr/sbin:/usr/bin:/sbin:/bin
MODE=/etc/pi-wifi-mode
HITS=/run/pi-wifi-hits
RETRY=/run/pi-wifi-retry
action=${1:-status}
exec 9>/run/pi-wifi.lock
if [ "$action" = check ]; then
    flock -n 9 || exit 0
else
    flock -w 100 9 || exit 1
fi
case "$action" in
status)
    printf 'Mode: '
    cat "$MODE" 2>/dev/null || printf 'auto\n'
    nmcli -f DEVICE,STATE,CONNECTION device status
    exit 0 ;;
auto)
    nmcli connection modify REPLACE_HOME_PROFILE connection.autoconnect yes
    nmcli connection modify REPLACE_ROUTER_PROFILE connection.autoconnect yes
    printf 'auto\n' >"$MODE"
    rm -f "$HITS" "$RETRY"
    echo 'Automatic switching enabled.'
    exit 0 ;;
home|m1)
    printf '%s\n' "$action" >"$MODE"
    rm -f "$HITS" "$RETRY"
    if [ "$action" = home ]; then
        target=REPLACE_HOME_PROFILE
        other=REPLACE_ROUTER_PROFILE
    else
        target=REPLACE_ROUTER_PROFILE
        other=REPLACE_HOME_PROFILE
    fi
    nmcli connection modify "$other" connection.autoconnect no
    nmcli connection modify "$target" connection.autoconnect yes
    nmcli --wait 30 connection up "$target" ifname wlan1
    exit 0 ;;
check) ;;
*)
    echo 'Usage: pi-wifi {status|auto|home|m1}' >&2
    exit 1 ;;
esac
mode=$(cat "$MODE" 2>/dev/null || printf 'auto')
[ "$mode" = auto ] || exit 0
state=$(nmcli -g GENERAL.STATE device show wlan1)
case "$state" in
    100*|30*|120*) ;;
    *) exit 0 ;;
esac
current=$(nmcli -g GENERAL.CONNECTION device show wlan1)
case "$current" in
    REPLACE_HOME_PROFILE) rm -f "$HITS"; exit 0 ;;
    REPLACE_ROUTER_PROFILE|--|'') ;;
    *) exit 0 ;;
esac
now=$(awk '{printf "%d", $1}' /proc/uptime)
retry=$(cat "$RETRY" 2>/dev/null || printf '0')
[ "$now" -ge "$retry" ] || exit 0
if ! scan=$(nmcli --wait 20 -t -f SSID,SIGNAL device wifi list ifname wlan1 --rescan yes); then
    rm -f "$HITS"
    exit 0
fi
signal=$(printf '%s\n' "$scan" |
    awk -F: '$1=="REPLACE_HOME_SSID" && $2>best {best=$2} END {print best+0}')
if [ "$signal" -lt 40 ]; then
    rm -f "$HITS"
    exit 0
fi
hits=$(cat "$HITS" 2>/dev/null || printf '0')
hits=$((hits + 1))
printf '%s\n' "$hits" >"$HITS"
[ "$hits" -ge 2 ] || exit 0
rm -f "$HITS"
printf '%s\n' "$((now + 300))" >"$RETRY"
echo 'Home Wi-Fi detected; switching to home profile.'
if ! nmcli --wait 30 connection up REPLACE_HOME_PROFILE ifname wlan1; then
    echo 'Home connection failed; trying router profile.'
    nmcli --wait 30 connection up REPLACE_ROUTER_PROFILE ifname wlan1
fi
