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
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
class JwtMacWrapper implements PrimitiveWrapper<JwtMac, JwtMac> {
    private static final JwtMacWrapper WRAPPER = new JwtMacWrapper();

    private static class JwtMacWithId {
        final int id;
        final JwtMac jwtMac;

        JwtMacWithId(JwtMac jwtMac, int id) {
            this.jwtMac = jwtMac;
            this.id = id;
        }
    }

    private static void validate(PrimitiveSet<JwtMac> primitiveSet) throws GeneralSecurityException {
        if (primitiveSet.getKeysetHandle().getPrimary() == null) {
            throw new GeneralSecurityException("Primitive set has no primary.");
        }
    }

    @Immutable
    private static class WrappedJwtMac implements JwtMac {
        private final List<JwtMacWithId> allMacs;
        private final MonitoringClient.Logger computeLogger;
        private final JwtMacWithId primary;
        private final MonitoringClient.Logger verifyLogger;

        private WrappedJwtMac(JwtMacWithId primary, List<JwtMacWithId> allMacs, MonitoringClient.Logger computeLogger, MonitoringClient.Logger verifyLogger) {
            this.primary = primary;
            this.allMacs = allMacs;
            this.computeLogger = computeLogger;
            this.verifyLogger = verifyLogger;
        }

        @Override // com.google.crypto.tink.jwt.JwtMac
        public String computeMacAndEncode(RawJwt token) throws GeneralSecurityException {
            try {
                String strComputeMacAndEncode = this.primary.jwtMac.computeMacAndEncode(token);
                this.computeLogger.log(this.primary.id, 1L);
                return strComputeMacAndEncode;
            } catch (GeneralSecurityException e) {
                this.computeLogger.logFailure();
                throw e;
            }
        }

        @Override // com.google.crypto.tink.jwt.JwtMac
        public VerifiedJwt verifyMacAndDecode(String compact, JwtValidator validator) throws GeneralSecurityException {
            GeneralSecurityException generalSecurityException = null;
            for (JwtMacWithId jwtMacWithId : this.allMacs) {
                try {
                    VerifiedJwt verifiedJwtVerifyMacAndDecode = jwtMacWithId.jwtMac.verifyMacAndDecode(compact, validator);
                    this.verifyLogger.log(jwtMacWithId.id, 1L);
                    return verifiedJwtVerifyMacAndDecode;
                } catch (GeneralSecurityException e) {
                    if (e instanceof JwtInvalidException) {
                        generalSecurityException = e;
                    }
                }
            }
            this.verifyLogger.logFailure();
            if (generalSecurityException != null) {
                throw generalSecurityException;
            }
            throw new GeneralSecurityException("invalid MAC");
        }
    }

    JwtMacWrapper() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public JwtMac wrap(final PrimitiveSet<JwtMac> primitives) throws GeneralSecurityException {
        MonitoringClient.Logger logger;
        MonitoringClient.Logger loggerCreateLogger;
        validate(primitives);
        KeysetHandleInterface keysetHandle = primitives.getKeysetHandle();
        ArrayList arrayList = new ArrayList(keysetHandle.size());
        for (int i = 0; i < keysetHandle.size(); i++) {
            KeysetHandleInterface.Entry at = keysetHandle.getAt(i);
            arrayList.add(new JwtMacWithId(primitives.getPrimitiveForEntry(at), at.getId()));
        }
        if (!primitives.getAnnotations().isEmpty()) {
            MonitoringClient monitoringClient = MutableMonitoringRegistry.globalInstance().getMonitoringClient();
            MonitoringClient.Logger loggerCreateLogger2 = monitoringClient.createLogger(keysetHandle, primitives.getAnnotations(), "jwtmac", "compute");
            loggerCreateLogger = monitoringClient.createLogger(keysetHandle, primitives.getAnnotations(), "jwtmac", "verify");
            logger = loggerCreateLogger2;
        } else {
            logger = MonitoringUtil.DO_NOTHING_LOGGER;
            loggerCreateLogger = MonitoringUtil.DO_NOTHING_LOGGER;
        }
        return new WrappedJwtMac(new JwtMacWithId(primitives.getPrimitiveForEntry(keysetHandle.getPrimary()), keysetHandle.getPrimary().getId()), arrayList, logger, loggerCreateLogger);
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<JwtMac> getPrimitiveClass() {
        return JwtMac.class;
    }

    @Override // com.google.crypto.tink.internal.PrimitiveWrapper
    public Class<JwtMac> getInputPrimitiveClass() {
        return JwtMac.class;
    }

    public static void register() throws GeneralSecurityException {
        MutablePrimitiveRegistry.globalInstance().registerPrimitiveWrapper(WRAPPER);
    }
}
