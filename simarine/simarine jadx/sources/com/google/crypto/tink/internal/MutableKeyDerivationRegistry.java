package com.google.crypto.tink.internal;

import com.google.crypto.tink.Key;
import com.google.crypto.tink.Parameters;
import com.google.crypto.tink.SecretKeyAccess;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes2.dex */
public final class MutableKeyDerivationRegistry {
    private static final MutableKeyDerivationRegistry globalInstance = new MutableKeyDerivationRegistry();
    private final Map<Class<? extends Parameters>, InsecureKeyCreator<? extends Parameters>> creators = new HashMap();

    public interface InsecureKeyCreator<ParametersT extends Parameters> {
        Key createKeyFromRandomness(ParametersT parameters, InputStream inputStream, @Nullable Integer idRequirement, SecretKeyAccess access) throws GeneralSecurityException;
    }

    public static MutableKeyDerivationRegistry globalInstance() {
        return globalInstance;
    }

    public synchronized <ParametersT extends Parameters> void add(InsecureKeyCreator<ParametersT> creator, Class<ParametersT> parametersClass) throws GeneralSecurityException {
        InsecureKeyCreator<? extends Parameters> insecureKeyCreator = this.creators.get(parametersClass);
        if (insecureKeyCreator != null && !insecureKeyCreator.equals(creator)) {
            throw new GeneralSecurityException("Different key creator for parameters class already inserted");
        }
        this.creators.put(parametersClass, creator);
    }

    public Key createKeyFromRandomness(Parameters parameters, InputStream inputStream, @Nullable Integer idRequirement, SecretKeyAccess access) throws GeneralSecurityException {
        return createKeyFromRandomnessTyped(parameters, inputStream, idRequirement, access);
    }

    private synchronized <ParametersT extends Parameters> Key createKeyFromRandomnessTyped(ParametersT parameters, InputStream inputStream, @Nullable Integer idRequirement, SecretKeyAccess access) throws GeneralSecurityException {
        InsecureKeyCreator<? extends Parameters> insecureKeyCreator;
        insecureKeyCreator = this.creators.get(parameters.getClass());
        if (insecureKeyCreator == null) {
            throw new GeneralSecurityException("Cannot use key derivation to derive key for parameters " + parameters + ": no key creator for this class was registered.");
        }
        return insecureKeyCreator.createKeyFromRandomness(parameters, inputStream, idRequirement, access);
    }
}
