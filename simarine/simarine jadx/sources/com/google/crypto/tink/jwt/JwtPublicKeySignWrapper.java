package com.google.crypto.tink.jwt;

import com.google.crypto.tink.internal.KeysetHandleInterface;
import com.google.crypto.tink.internal.MonitoringClient;
import com.google.crypto.tink.internal.MonitoringUtil;
import com.google.crypto.tink.internal.MutableMonitoringRegistry;
import com.google.crypto.tink.internal.MutablePrimitiveRegistry;
import com.google.crypto.tink.internal.PrimitiveSet;
import com.google.crypto.tink.internal.PrimitiveWrapper;
import com.google.errorprone.annotations.Immutable;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes2.dex */
class JwtPublicKeySignWrapper implements PrimitiveWrapper<JwtPublicKeySign, JwtPublicKeySign> {
    private static final JwtPublicKeySignWrapper WRAPPER = new JwtPublicKeySignWrapper();

    JwtPublicKeySignWrapper() {
    }

    @Immutable
    private static class WrappedJwtPublicKeySign implements JwtPublicKeySign {
        private final MonitoringClient.Logger logger;
        private final JwtPublicKeySign primary;
        private final int primaryKeyId;

        public WrappedJwtPublicKeySign(final PrimitiveSet<JwtPublicKeySign> primitives) throws GeneralSecurityException {
            KeysetHandleInterface keysetHandle = primitives.getKeysetHandle();
            this.primary = primitives.getPrimitiveForEntry(keysetHandle.getPrimary());
            this.primaryKeyId = keysetHandle.getPrimary().getId();
            if (!primitives.getAnnotations().isEmpty()) {
                this.logger = MutableMonitoringRegistry.globalInstance().getMonitoringClient().createLogger(keysetHandle, primitives.getAnnotations(), "jwtsign", "sign");
            } else {
                this.logger = MonitoringUtil.DO_NOTHING_LOGGER;
            }
        }

        @Override // com.google.crypto.tink.jwt.JwtPublicKeySign
        public String signAndEncode(RawJwt token) throws GeneralSecurityException {
            try {
                String strSignAndEncode = this.primary.signAndEncode(token);
                this.logger.log(this.primaryKeyId, 1L);
                return strSignAndEncode;
            } catch (GeneralSecurityException e) {
                this.logger.logFailure();
                throw e;
            }
        }
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public JwtPublicKeySign wrap(final PrimitiveSet<JwtPublicKeySign> primitives) throws GeneralSecurityException {
        return new WrappedJwtPublicKeySign(primitives);
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<JwtPublicKeySign> getPrimitiveClass() {
        return JwtPublicKeySign.class;
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<JwtPublicKeySign> getInputPrimitiveClass() {
        return JwtPublicKeySign.class;
    }

    public static void register() throws GeneralSecurityException {
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveWrapper(WRAPPER);
    }
}
