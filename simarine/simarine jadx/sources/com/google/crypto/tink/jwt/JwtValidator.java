package com.google.crypto.tink.jwt;

import com.google.errorprone.annotations.Immutable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Optional;

/* JADX INFO: loaded from: classes2.dex */
@Immutable
public final class JwtValidator {
    private static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(10);
    private final boolean allowMissingExpiration;
    private final Clock clock;
    private final Duration clockSkew;
    private final boolean expectIssuedInThePast;
    private final Optional<String> expectedAudience;
    private final Optional<String> expectedIssuer;
    private final Optional<String> expectedTypeHeader;
    private final boolean ignoreAudiences;
    private final boolean ignoreIssuer;
    private final boolean ignoreTypeHeader;

    private JwtValidator(Builder builder) {
        this.expectedTypeHeader = builder.expectedTypeHeader;
        this.ignoreTypeHeader = builder.ignoreTypeHeader;
        this.expectedIssuer = builder.expectedIssuer;
        this.ignoreIssuer = builder.ignoreIssuer;
        this.expectedAudience = builder.expectedAudience;
        this.ignoreAudiences = builder.ignoreAudiences;
        this.allowMissingExpiration = builder.allowMissingExpiration;
        this.expectIssuedInThePast = builder.expectIssuedInThePast;
        this.clock = builder.clock;
        this.clockSkew = builder.clockSkew;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean allowMissingExpiration;
        private Clock clock;
        private Duration clockSkew;
        private boolean expectIssuedInThePast;
        private Optional<String> expectedAudience;
        private Optional<String> expectedIssuer;
        private Optional<String> expectedTypeHeader;
        private boolean ignoreAudiences;
        private boolean ignoreIssuer;
        private boolean ignoreTypeHeader;

        private Builder() {
            this.clock = Clock.systemUTC();
            this.clockSkew = Duration.ZERO;
            this.expectedTypeHeader = Optional.empty();
            this.ignoreTypeHeader = false;
            this.expectedIssuer = Optional.empty();
            this.ignoreIssuer = false;
            this.expectedAudience = Optional.empty();
            this.ignoreAudiences = false;
            this.allowMissingExpiration = false;
            this.expectIssuedInThePast = false;
        }

        public Builder expectTypeHeader(String value) {
            if (value == null) {
                throw new NullPointerException("typ header cannot be null");
            }
            this.expectedTypeHeader = Optional.of(value);
            return this;
        }

        public Builder ignoreTypeHeader() {
            this.ignoreTypeHeader = true;
            return this;
        }

        public Builder expectIssuer(String value) {
            if (value == null) {
                throw new NullPointerException("issuer cannot be null");
            }
            this.expectedIssuer = Optional.of(value);
            return this;
        }

        public Builder ignoreIssuer() {
            this.ignoreIssuer = true;
            return this;
        }

        public Builder expectAudience(String value) {
            if (value == null) {
                throw new NullPointerException("audience cannot be null");
            }
            this.expectedAudience = Optional.of(value);
            return this;
        }

        public Builder ignoreAudiences() {
            this.ignoreAudiences = true;
            return this;
        }

        public Builder expectIssuedInThePast() {
            this.expectIssuedInThePast = true;
            return this;
        }

        public Builder setClock(Clock clock) {
            if (clock == null) {
                throw new NullPointerException("clock cannot be null");
            }
            this.clock = clock;
            return this;
        }

        public Builder setClockSkew(Duration clockSkew) {
            if (clockSkew.compareTo(JwtValidator.MAX_CLOCK_SKEW) > 0) {
                throw new IllegalArgumentException("Clock skew too large, max is 10 minutes");
            }
            this.clockSkew = clockSkew;
            return this;
        }

        public Builder allowMissingExpiration() {
            this.allowMissingExpiration = true;
            return this;
        }

        public JwtValidator build() {
            if (this.ignoreTypeHeader && this.expectedTypeHeader.isPresent()) {
                throw new IllegalArgumentException("ignoreTypeHeader() and expectedTypeHeader() cannot be used together.");
            }
            if (this.ignoreIssuer && this.expectedIssuer.isPresent()) {
                throw new IllegalArgumentException("ignoreIssuer() and expectedIssuer() cannot be used together.");
            }
            if (this.ignoreAudiences && this.expectedAudience.isPresent()) {
                throw new IllegalArgumentException("ignoreAudiences() and expectedAudience() cannot be used together.");
            }
            return new JwtValidator(this);
        }
    }

    private void validateTypeHeader(RawJwt target) throws JwtInvalidException {
        if (this.expectedTypeHeader.isPresent()) {
            if (!target.hasTypeHeader()) {
                throw new JwtInvalidException(String.format("invalid JWT; missing expected type header %s.", this.expectedTypeHeader.get()));
            }
            if (!target.getTypeHeader().equals(this.expectedTypeHeader.get())) {
                throw new JwtInvalidException(String.format("invalid JWT; expected type header %s, but got %s", this.expectedTypeHeader.get(), target.getTypeHeader()));
            }
            return;
        }
        if (target.hasTypeHeader() && !this.ignoreTypeHeader) {
            throw new JwtInvalidException("invalid JWT; token has type header set, but validator not.");
        }
    }

    private void validateIssuer(RawJwt target) throws JwtInvalidException {
        if (this.expectedIssuer.isPresent()) {
            if (!target.hasIssuer()) {
                throw new JwtInvalidException(String.format("invalid JWT; missing expected issuer %s.", this.expectedIssuer.get()));
            }
            if (!target.getIssuer().equals(this.expectedIssuer.get())) {
                throw new JwtInvalidException(String.format("invalid JWT; expected issuer %s, but got %s", this.expectedIssuer.get(), target.getIssuer()));
            }
            return;
        }
        if (target.hasIssuer() && !this.ignoreIssuer) {
            throw new JwtInvalidException("invalid JWT; token has issuer set, but validator not.");
        }
    }

    private void validateAudiences(RawJwt target) throws JwtInvalidException {
        if (this.expectedAudience.isPresent()) {
            if (!target.hasAudiences() || !target.getAudiences().contains(this.expectedAudience.get())) {
                throw new JwtInvalidException(String.format("invalid JWT; missing expected audience %s.", this.expectedAudience.get()));
            }
        } else if (target.hasAudiences() && !this.ignoreAudiences) {
            throw new JwtInvalidException("invalid JWT; token has audience set, but validator not.");
        }
    }

    VerifiedJwt validate(RawJwt target) throws JwtInvalidException {
        validateTimestampClaims(target);
        validateTypeHeader(target);
        validateIssuer(target);
        validateAudiences(target);
        return new VerifiedJwt(target);
    }

    private void validateTimestampClaims(RawJwt target) throws JwtInvalidException {
        Instant instant = this.clock.instant();
        if (!target.hasExpiration() && !this.allowMissingExpiration) {
            throw new JwtInvalidException("token does not have an expiration set");
        }
        if (target.hasExpiration() && !target.getExpiration().isAfter(instant.minus((TemporalAmount) this.clockSkew))) {
            throw new JwtInvalidException("token has expired since " + target.getExpiration());
        }
        if (target.hasNotBefore() && target.getNotBefore().isAfter(instant.plus((TemporalAmount) this.clockSkew))) {
            throw new JwtInvalidException("token cannot be used before " + target.getNotBefore());
        }
        if (this.expectIssuedInThePast) {
            if (!target.hasIssuedAt()) {
                throw new JwtInvalidException("token does not have an iat claim");
            }
            if (target.getIssuedAt().isAfter(instant.plus((TemporalAmount) this.clockSkew))) {
                throw new JwtInvalidException("token has a invalid iat claim in the future: " + target.getIssuedAt());
            }
        }
    }

    public String toString() {
        ArrayList<String> arrayList = new ArrayList();
        if (this.expectedTypeHeader.isPresent()) {
            arrayList.add("expectedTypeHeader=" + this.expectedTypeHeader.get());
        }
        if (this.ignoreTypeHeader) {
            arrayList.add("ignoreTypeHeader");
        }
        if (this.expectedIssuer.isPresent()) {
            arrayList.add("expectedIssuer=" + this.expectedIssuer.get());
        }
        if (this.ignoreIssuer) {
            arrayList.add("ignoreIssuer");
        }
        if (this.expectedAudience.isPresent()) {
            arrayList.add("expectedAudience=" + this.expectedAudience.get());
        }
        if (this.ignoreAudiences) {
            arrayList.add("ignoreAudiences");
        }
        if (this.allowMissingExpiration) {
            arrayList.add("allowMissingExpiration");
        }
        if (this.expectIssuedInThePast) {
            arrayList.add("expectIssuedInThePast");
        }
        if (!this.clockSkew.isZero()) {
            arrayList.add("clockSkew=" + this.clockSkew);
        }
        StringBuilder sb = new StringBuilder("JwtValidator{");
        String str = "";
        for (String str2 : arrayList) {
            sb.append(str);
            sb.append(str2);
            str = ",";
        }
        sb.append("}");
        return sb.toString();
    }
}
