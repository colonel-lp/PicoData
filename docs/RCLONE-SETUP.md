# Rclone installation and Google Drive setup

For a headless Raspberry Pi, using a Mac browser for Google sign-in. Reviewed 28 October 2026. This prepares cloud access; it does not schedule database backups.

## 1. Install on the Pi

```bash
sudo apt update
sudo apt install curl unzip
curl -fsSL https://rclone.org/install.sh | sudo bash
rclone version
```

Use the normal Pi login account for configuration and future backup jobs; run `rclone config` without `sudo`.

## 2. Create Google credentials on the Mac

1. In [Google Cloud Console](https://console.cloud.google.com/), select or create **Ella Backups**, then enable **Google Drive API** under **APIs & Services → Library**.
2. Open [Google Auth Platform](https://console.cloud.google.com/auth/overview). If prompted, choose **Get started**; enter **Ella Backups**, your support/contact email, and **External** audience. **Internal** is for Google Workspace organisations.
3. Under **Data Access**, add only:
   ```text
   https://www.googleapis.com/auth/drive.file
   ```
4. Publish the repository's [information pages](README.md#publish-with-github-pages). Once both pages load, complete **Branding** with:

   | Field | Value |
   | --- | --- |
   | Application home page | https://colonel-lp.github.io/PicoData/ |
   | Privacy policy | https://colonel-lp.github.io/PicoData/privacy.html |
   | Authorised domain | `colonel-lp.github.io` |
5. Under **Clients**, create an OAuth client of type **Desktop app**. Keep its **Client ID** and **Client secret** privately.
6. Under **Audience**, choose **Publish app** to switch to Production.

**Publishing blocked despite all starred fields being filled?** Save the homepage URL, privacy-policy URL and authorised domain in Branding, then return to Audience. These links are also publishing requirements.

Use your own client credentials: rclone's shared Google client is being retired during 2026. External/Testing authorisations expire after seven days; switch to Production before authorising for unattended use.

## 3. Authenticate through an SSH tunnel

On the **Mac**, replace `YOUR_USER` and `PI_HOST` below with the Pi login and hostname/address:

```bash
ssh -L 127.0.0.1:53682:127.0.0.1:53682 YOUR_USER@PI_HOST
```

Keep that session open. At its **Pi shell prompt**, run:

```bash
rclone config
```

| Prompt | Answer |
| --- | --- |
| New remote | `n` |
| Name | `gdrive` |
| Storage type | `drive` |
| Client ID / client secret | Your Google credentials |
| Scope | `drive.file` — files created by rclone only |
| Root folder / service account file, if offered | Leave blank |
| Advanced configuration | `n` |
| Use browser to authenticate | **`y`**, because the SSH tunnel is open |
| Shared / Team Drive | `n` |
| Keep this remote | `y` |

When rclone displays its `http://127.0.0.1:53682/...` authentication URL, open the **exact displayed URL in the Mac browser**. Sign in to the Google account that will hold the backups and authorise access, then finish the remaining prompts on the Pi.

## 4. Check access on the Pi

```bash
rclone mkdir gdrive:ella-monitoring-backups
rclone lsd gdrive:
```

The listing should contain `ella-monitoring-backups`. Create the folder through rclone: `drive.file` cannot see unrelated folders created through the Drive website. OAuth tokens refresh automatically during normal use unless access expires or is revoked.

Keep the client secret, OAuth tokens and rclone configuration out of this public repository and screenshots. For a future backup job, upload a consistent SQLite snapshot; do not upload the live database file alone.

## Official references

- [Installation](https://rclone.org/install/)
- [Headless setup using an SSH tunnel](https://rclone.org/remote_setup/#configuring-using-ssh-tunnel)
- [Google Drive client credentials](https://rclone.org/drive/#making-your-own-client-id)
- [Drive file scope](https://rclone.org/drive/#drive-file)
