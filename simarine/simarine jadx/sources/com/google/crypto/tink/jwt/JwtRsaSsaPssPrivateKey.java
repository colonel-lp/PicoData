package com.google.crypto.tink.jwt;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.util.SecretBigInteger;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Optional;

/* JADX INFO: loaded from: classes2.dex */
public final class JwtRsaSsaPssPrivateKey extends JwtSignaturePrivateKey {
    private final SecretBigInteger d;
    private final SecretBigInteger dP;
    private final SecretBigInteger dQ;
    private final SecretBigInteger p;
    private final JwtRsaSsaPssPublicKey publicKey;
    private final SecretBigInteger q;
    private final SecretBigInteger qInv;

    public static class Builder {
        private static final int PRIME_CERTAINTY = 10;
        private Optional<SecretBigInteger> d;
        private Optional<SecretBigInteger> dP;
        private Optional<SecretBigInteger> dQ;
        private Optional<SecretBigInteger> p;
        private Optional<JwtRsaSsaPssPublicKey> publicKey;
        private Optional<SecretBigInteger> q;
        private Optional<SecretBigInteger> qInv;

        private Builder() {
            this.publicKey = Optional.empty();
            this.d = Optional.empty();
            this.p = Optional.empty();
            this.q = Optional.empty();
            this.dP = Optional.empty();
            this.dQ = Optional.empty();
            this.qInv = Optional.empty();
        }

        public Builder setPublicKey(JwtRsaSsaPssPublicKey publicKey) {
            this.publicKey = Optional.of(publicKey);
            return this;
        }

        public Builder setPrimes(SecretBigInteger p, SecretBigInteger q) {
            this.p = Optional.of(p);
            this.q = Optional.of(q);
            return this;
        }

        public Builder setPrivateExponent(SecretBigInteger d) {
            this.d = Optional.of(d);
            return this;
        }

        public Builder setPrimeExponents(SecretBigInteger dP, SecretBigInteger dQ) {
            this.dP = Optional.of(dP);
            this.dQ = Optional.of(dQ);
            return this;
        }

        public Builder setCrtCoefficient(SecretBigInteger qInv) {
            this.qInv = Optional.of(qInv);
            return this;
        }

        public JwtRsaSsaPssPrivateKey build() throws GeneralSecurityException {
            if (!this.publicKey.isPresent()) {
                throw new GeneralSecurityException("Cannot build without a RSA SSA PSS public key");
            }
            if (!this.p.isPresent() || !this.q.isPresent()) {
                throw new GeneralSecurityException("Cannot build without prime factors");
            }
            if (!this.d.isPresent()) {
                throw new GeneralSecurityException("Cannot build without private exponent");
            }
            if (!this.dP.isPresent() || !this.dQ.isPresent()) {
                throw new GeneralSecurityException("Cannot build without prime exponents");
            }
            if (!this.qInv.isPresent()) {
                throw new GeneralSecurityException("Cannot build without CRT coefficient");
            }
            BigInteger publicExponent = this.publicKey.get().getParameters().getPublicExponent();
            BigInteger modulus = this.publicKey.get().getModulus();
            BigInteger bigInteger = this.p.get().getBigInteger(InsecureSecretKeyAccess.get());
            BigInteger bigInteger2 = this.q.get().getBigInteger(InsecureSecretKeyAccess.get());
            BigInteger bigInteger3 = this.d.get().getBigInteger(InsecureSecretKeyAccess.get());
            BigInteger bigInteger4 = this.dP.get().getBigInteger(InsecureSecretKeyAccess.get());
            BigInteger bigInteger5 = this.dQ.get().getBigInteger(InsecureSecretKeyAccess.get());
            BigInteger bigInteger6 = this.qInv.get().getBigInteger(InsecureSecretKeyAccess.get());
            if (!bigInteger.isProbablePrime(10)) {
                throw new GeneralSecurityException("p is not a prime");
            }
            if (!bigInteger2.isProbablePrime(10)) {
                throw new GeneralSecurityException("q is not a prime");
            }
            if (!bigInteger.multiply(bigInteger2).equals(modulus)) {
                throw new GeneralSecurityException("Prime p times prime q is not equal to the public key's modulus");
            }
            BigInteger bigIntegerSubtract = bigInteger.subtract(BigInteger.ONE);
            BigInteger bigIntegerSubtract2 = bigInteger2.subtract(BigInteger.ONE);
            if (!publicExponent.multiply(bigInteger3).mod(bigIntegerSubtract.divide(bigIntegerSubtract.gcd(bigIntegerSubtract2)).multiply(bigIntegerSubtract2)).equals(BigInteger.ONE)) {
                throw new GeneralSecurityException("D is invalid.");
            }
            if (!publicExponent.multiply(bigInteger4).mod(bigIntegerSubtract).equals(BigInteger.ONE)) {
                throw new GeneralSecurityException("dP is invalid.");
            }
            if (!publicExponent.multiply(bigInteger5).mod(bigIntegerSubtract2).equals(BigInteger.ONE)) {
                throw new GeneralSecurityException("dQ is invalid.");
            }
            if (!bigInteger2.multiply(bigInteger6).mod(bigInteger).equals(BigInteger.ONE)) {
                throw new GeneralSecurityException("qInv is invalid.");
            }
            return new JwtRsaSsaPssPrivateKey(this.publicKey.get(), this.p.get(), this.q.get(), this.d.get(), this.dP.get(), this.dQ.get(), this.qInv.get());
        }
    }

    private JwtRsaSsaPssPrivateKey(JwtRsaSsaPssPublicKey publicKey, SecretBigInteger p, SecretBigInteger q, SecretBigInteger d, SecretBigInteger dP, SecretBigInteger dQ, SecretBigInteger qInv) {
        this.publicKey = publicKey;
        this.p = p;
        this.q = q;
        this.d = d;
        this.dP = dP;
        this.dQ = dQ;
        this.qInv = qInv;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override // com.google.crypto.tink.jwt.JwtSignaturePrivateKey, com.google.crypto.tink.Key
    public JwtRsaSsaPssParameters getParameters() {
        return this.publicKey.getParameters();
    }

    @Override // com.google.crypto.tink.jwt.JwtSignaturePrivateKey, com.google.crypto.tink.PrivateKey
    public JwtRsaSsaPssPublicKey getPublicKey() {
        return this.publicKey;
    }

    public SecretBigInteger getPrimeP() {
        return this.p;
    }

    public SecretBigInteger getPrimeQ() {
        return this.q;
    }

    public SecretBigInteger getPrivateExponent() {
        return this.d;
    }

    public SecretBigInteger getPrimeExponentP() {
        return this.dP;
    }

    public SecretBigInteger getPrimeExponentQ() {
        return this.dQ;
    }

    public SecretBigInteger getCrtCoefficient() {
        return this.qInv;
    }

    @Override // com.google.crypto.tink.Key
    public boolean equalsKey(Key o) {
        if (!(o instanceof JwtRsaSsaPssPrivateKey)) {
            return false;
        }
        JwtRsaSsaPssPrivateKey jwtRsaSsaPssPrivateKey = (JwtRsaSsaPssPrivateKey) o;
        return jwtRsaSsaPssPrivateKey.publicKey.equalsKey(this.publicKey) && this.p.equalsSecretBigInteger(jwtRsaSsaPssPrivateKey.p) && this.q.equalsSecretBigInteger(jwtRsaSsaPssPrivateKey.q) && this.d.equalsSecretBigInteger(jwtRsaSsaPssPrivateKey.d) && this.dP.equalsSecretBigInteger(jwtRsaSsaPssPrivateKey.dP) && this.dQ.equalsSecretBigInteger(jwtRsaSsaPssPrivateKey.dQ) && this.qInv.equalsSecretBigInteger(jwtRsaSsaPssPrivateKey.qInv);
    }
}
