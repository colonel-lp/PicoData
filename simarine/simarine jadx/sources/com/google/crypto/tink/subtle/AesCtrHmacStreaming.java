package com.google.crypto.tink.subtle;

import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.StreamingAead;
import com.google.crypto.tink.config.internal.TinkFipsUtil;
import com.google.crypto.tink.streamingaead.AesCtrHmacStreamingKey;
import com.google.crypto.tink.streamingaead.AesCtrHmacStreamingParameters;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes2.dex */
public final class AesCtrHmacStreaming extends NonceBasedStreamingAead {
    public static final TinkFipsUtil.AlgorithmFipsCompatibility FIPS = TinkFipsUtil.AlgorithmFipsCompatibility.ALGORITHM_NOT_FIPS;
    private static final int HMAC_KEY_SIZE_IN_BYTES = 32;
    private static final int NONCE_PREFIX_IN_BYTES = 7;
    private static final int NONCE_SIZE_IN_BYTES = 16;
    private final int ciphertextSegmentSize;
    private final int firstSegmentOffset;
    private final String hkdfAlgo;
    private final byte[] ikm;
    private final int keySizeInBytes;
    private final int plaintextSegmentSize;
    private final String tagAlgo;
    private final int tagSizeInBytes;

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead, com.google.crypto.tink.StreamingAead
    public /* bridge */ /* synthetic */ ReadableByteChannel newDecryptingChannel(ReadableByteChannel ciphertextChannel, byte[] associatedData) throws GeneralSecurityException, IOException {
        return super.newDecryptingChannel(ciphertextChannel, associatedData);
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead, com.google.crypto.tink.StreamingAead
    public /* bridge */ /* synthetic */ InputStream newDecryptingStream(InputStream ciphertextStream, byte[] associatedData) throws GeneralSecurityException, IOException {
        return super.newDecryptingStream(ciphertextStream, associatedData);
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead, com.google.crypto.tink.StreamingAead
    public /* bridge */ /* synthetic */ WritableByteChannel newEncryptingChannel(WritableByteChannel ciphertextChannel, byte[] associatedData) throws GeneralSecurityException, IOException {
        return super.newEncryptingChannel(ciphertextChannel, associatedData);
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead, com.google.crypto.tink.StreamingAead
    public /* bridge */ /* synthetic */ OutputStream newEncryptingStream(OutputStream ciphertext, byte[] associatedData) throws GeneralSecurityException, IOException {
        return super.newEncryptingStream(ciphertext, associatedData);
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead, com.google.crypto.tink.StreamingAead
    public /* bridge */ /* synthetic */ SeekableByteChannel newSeekableDecryptingChannel(SeekableByteChannel ciphertextSource, byte[] associatedData) throws GeneralSecurityException, IOException {
        return super.newSeekableDecryptingChannel(ciphertextSource, associatedData);
    }

    public AesCtrHmacStreaming(byte[] ikm, String hkdfAlgo, int keySizeInBytes, String tagAlgo, int tagSizeInBytes, int ciphertextSegmentSize, int firstSegmentOffset) throws GeneralSecurityException {
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-CTR-HMAC streaming in FIPS-mode.");
        }
        validateParameters(ikm.length, keySizeInBytes, tagAlgo, tagSizeInBytes, ciphertextSegmentSize, firstSegmentOffset);
        this.ikm = Arrays.copyOf(ikm, ikm.length);
        this.hkdfAlgo = hkdfAlgo;
        this.keySizeInBytes = keySizeInBytes;
        this.tagAlgo = tagAlgo;
        this.tagSizeInBytes = tagSizeInBytes;
        this.ciphertextSegmentSize = ciphertextSegmentSize;
        this.firstSegmentOffset = firstSegmentOffset;
        this.plaintextSegmentSize = ciphertextSegmentSize - tagSizeInBytes;
    }

    private AesCtrHmacStreaming(AesCtrHmacStreamingKey key) throws GeneralSecurityException {
        String str;
        if (!FIPS.isCompatible()) {
            throw new GeneralSecurityException("Can not use AES-CTR-HMAC streaming in FIPS-mode.");
        }
        this.ikm = key.getInitialKeyMaterial().toByteArray(InsecureSecretKeyAccess.get());
        String str2 = "HmacSha512";
        if (key.getParameters().getHkdfHashType().equals(AesCtrHmacStreamingParameters.HashType.SHA1)) {
            str = "HmacSha1";
        } else if (key.getParameters().getHkdfHashType().equals(AesCtrHmacStreamingParameters.HashType.SHA256)) {
            str = "HmacSha256";
        } else {
            str = key.getParameters().getHkdfHashType().equals(AesCtrHmacStreamingParameters.HashType.SHA512) ? "HmacSha512" : "";
        }
        this.hkdfAlgo = str;
        this.keySizeInBytes = key.getParameters().getDerivedKeySizeBytes();
        if (key.getParameters().getHmacHashType().equals(AesCtrHmacStreamingParameters.HashType.SHA1)) {
            str2 = "HmacSha1";
        } else if (key.getParameters().getHmacHashType().equals(AesCtrHmacStreamingParameters.HashType.SHA256)) {
            str2 = "HmacSha256";
        } else if (!key.getParameters().getHmacHashType().equals(AesCtrHmacStreamingParameters.HashType.SHA512)) {
            str2 = "";
        }
        this.tagAlgo = str2;
        int hmacTagSizeBytes = key.getParameters().getHmacTagSizeBytes();
        this.tagSizeInBytes = hmacTagSizeBytes;
        int ciphertextSegmentSizeBytes = key.getParameters().getCiphertextSegmentSizeBytes();
        this.ciphertextSegmentSize = ciphertextSegmentSizeBytes;
        this.firstSegmentOffset = 0;
        this.plaintextSegmentSize = ciphertextSegmentSizeBytes - hmacTagSizeBytes;
    }

    public static StreamingAead create(AesCtrHmacStreamingKey key) throws GeneralSecurityException {
        return new AesCtrHmacStreaming(key);
    }

    private static void validateParameters(int ikmSize, int keySizeInBytes, String tagAlgo, int tagSizeInBytes, int ciphertextSegmentSize, int firstSegmentOffset) throws InvalidAlgorithmParameterException {
        if (ikmSize < 16 || ikmSize < keySizeInBytes) {
            throw new InvalidAlgorithmParameterException("ikm too short, must be >= " + Math.max(16, keySizeInBytes));
        }
        if (firstSegmentOffset < 0) {
            throw new InvalidAlgorithmParameterException("firstSegmentOffset must not be negative");
        }
        Validators.validateAesKeySize(keySizeInBytes);
        if (tagSizeInBytes < 10) {
            throw new InvalidAlgorithmParameterException("tag size too small " + tagSizeInBytes);
        }
        if ((tagAlgo.equals("HmacSha1") && tagSizeInBytes > 20) || ((tagAlgo.equals("HmacSha256") && tagSizeInBytes > 32) || (tagAlgo.equals("HmacSha512") && tagSizeInBytes > 64))) {
            throw new InvalidAlgorithmParameterException("tag size too big");
        }
        if ((((ciphertextSegmentSize - firstSegmentOffset) - tagSizeInBytes) - keySizeInBytes) - 8 <= 0) {
            throw new InvalidAlgorithmParameterException("ciphertextSegmentSize too small");
        }
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public AesCtrHmacStreamEncrypter newStreamSegmentEncrypter(byte[] aad) throws GeneralSecurityException {
        return new AesCtrHmacStreamEncrypter(aad);
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public AesCtrHmacStreamDecrypter newStreamSegmentDecrypter() throws GeneralSecurityException {
        return new AesCtrHmacStreamDecrypter();
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public int getCiphertextSegmentSize() {
        return this.ciphertextSegmentSize;
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public int getPlaintextSegmentSize() {
        return this.plaintextSegmentSize;
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public int getHeaderLength() {
        return this.keySizeInBytes + 8;
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public int getCiphertextOffset() {
        return getHeaderLength() + this.firstSegmentOffset;
    }

    @Override // com.google.crypto.tink.subtle.NonceBasedStreamingAead
    public int getCiphertextOverhead() {
        return this.tagSizeInBytes;
    }

    public int getFirstSegmentOffset() {
        return this.firstSegmentOffset;
    }

    public long expectedCiphertextSize(long plaintextSize) {
        long ciphertextOffset = plaintextSize + ((long) getCiphertextOffset());
        int i = this.plaintextSegmentSize;
        long j = (ciphertextOffset / ((long) i)) * ((long) this.ciphertextSegmentSize);
        long j2 = ciphertextOffset % ((long) i);
        return j2 > 0 ? j + j2 + ((long) this.tagSizeInBytes) : j;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Cipher cipherInstance() throws GeneralSecurityException {
        return EngineFactory.CIPHER.getInstance("AES/CTR/NoPadding");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Mac macInstance() throws GeneralSecurityException {
        return EngineFactory.MAC.getInstance(this.tagAlgo);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] randomSalt() {
        return Random.randBytes(this.keySizeInBytes);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] nonceForSegment(byte[] bArr, long j, boolean z) throws GeneralSecurityException {
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(16);
        byteBufferAllocate.order(ByteOrder.BIG_ENDIAN);
        byteBufferAllocate.put(bArr);
        SubtleUtil.putAsUnsigedInt(byteBufferAllocate, j);
        byteBufferAllocate.put(z ? (byte) 1 : (byte) 0);
        byteBufferAllocate.putInt(0);
        return byteBufferAllocate.array();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] randomNonce() {
        return Random.randBytes(7);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] deriveKeyMaterial(byte[] salt, byte[] aad) throws GeneralSecurityException {
        return Hkdf.computeHkdf(this.hkdfAlgo, this.ikm, salt, aad, this.keySizeInBytes + 32);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SecretKeySpec deriveKeySpec(byte[] keyMaterial) throws GeneralSecurityException {
        return new SecretKeySpec(keyMaterial, 0, this.keySizeInBytes, "AES");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SecretKeySpec deriveHmacKeySpec(byte[] keyMaterial) throws GeneralSecurityException {
        return new SecretKeySpec(keyMaterial, this.keySizeInBytes, 32, this.tagAlgo);
    }

    class AesCtrHmacStreamEncrypter implements StreamSegmentEncrypter {
        private final Cipher cipher = AesCtrHmacStreaming.cipherInstance();
        private long encryptedSegments;
        private ByteBuffer header;
        private final SecretKeySpec hmacKeySpec;
        private final SecretKeySpec keySpec;
        private final Mac mac;
        private final byte[] noncePrefix;

        public AesCtrHmacStreamEncrypter(byte[] aad) throws GeneralSecurityException {
            this.encryptedSegments = 0L;
            this.mac = AesCtrHmacStreaming.this.macInstance();
            this.encryptedSegments = 0L;
            byte[] bArrRandomSalt = AesCtrHmacStreaming.this.randomSalt();
            byte[] bArrRandomNonce = AesCtrHmacStreaming.this.randomNonce();
            this.noncePrefix = bArrRandomNonce;
            ByteBuffer byteBufferAllocate = ByteBuffer.allocate(AesCtrHmacStreaming.this.getHeaderLength());
            this.header = byteBufferAllocate;
            byteBufferAllocate.put((byte) AesCtrHmacStreaming.this.getHeaderLength());
            this.header.put(bArrRandomSalt);
            this.header.put(bArrRandomNonce);
            this.header.flip();
            byte[] bArrDeriveKeyMaterial = AesCtrHmacStreaming.this.deriveKeyMaterial(bArrRandomSalt, aad);
            this.keySpec = AesCtrHmacStreaming.this.deriveKeySpec(bArrDeriveKeyMaterial);
            this.hmacKeySpec = AesCtrHmacStreaming.this.deriveHmacKeySpec(bArrDeriveKeyMaterial);
        }

        @Override // com.google.crypto.tink.subtle.StreamSegmentEncrypter
        public ByteBuffer getHeader() {
            return this.header.asReadOnlyBuffer();
        }

        @Override // com.google.crypto.tink.subtle.StreamSegmentEncrypter
        public synchronized void encryptSegment(ByteBuffer plaintext, boolean isLastSegment, ByteBuffer ciphertext) throws GeneralSecurityException {
            int iPosition = ciphertext.position();
            byte[] bArrNonceForSegment = AesCtrHmacStreaming.this.nonceForSegment(this.noncePrefix, this.encryptedSegments, isLastSegment);
            this.cipher.init(1, this.keySpec, new IvParameterSpec(bArrNonceForSegment));
            this.encryptedSegments++;
            this.cipher.doFinal(plaintext, ciphertext);
            ByteBuffer byteBufferDuplicate = ciphertext.duplicate();
            byteBufferDuplicate.flip();
            byteBufferDuplicate.position(iPosition);
            this.mac.init(this.hmacKeySpec);
            this.mac.update(bArrNonceForSegment);
            this.mac.update(byteBufferDuplicate);
            ciphertext.put(this.mac.doFinal(), 0, AesCtrHmacStreaming.this.tagSizeInBytes);
        }

        @Override // com.google.crypto.tink.subtle.StreamSegmentEncrypter
        public synchronized void encryptSegment(ByteBuffer part1, ByteBuffer part2, boolean isLastSegment, ByteBuffer ciphertext) throws GeneralSecurityException {
            int iPosition = ciphertext.position();
            byte[] bArrNonceForSegment = AesCtrHmacStreaming.this.nonceForSegment(this.noncePrefix, this.encryptedSegments, isLastSegment);
            this.cipher.init(1, this.keySpec, new IvParameterSpec(bArrNonceForSegment));
            this.encryptedSegments++;
            this.cipher.update(part1, ciphertext);
            this.cipher.doFinal(part2, ciphertext);
            ByteBuffer byteBufferDuplicate = ciphertext.duplicate();
            byteBufferDuplicate.flip();
            byteBufferDuplicate.position(iPosition);
            this.mac.init(this.hmacKeySpec);
            this.mac.update(bArrNonceForSegment);
            this.mac.update(byteBufferDuplicate);
            ciphertext.put(this.mac.doFinal(), 0, AesCtrHmacStreaming.this.tagSizeInBytes);
        }
    }

    class AesCtrHmacStreamDecrypter implements StreamSegmentDecrypter {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private Cipher cipher;
        private SecretKeySpec hmacKeySpec;
        private SecretKeySpec keySpec;
        private Mac mac;
        private byte[] noncePrefix;

        AesCtrHmacStreamDecrypter() {
        }

        @Override // com.google.crypto.tink.subtle.StreamSegmentDecrypter
        public synchronized void init(ByteBuffer header, byte[] aad) throws GeneralSecurityException {
            if (header.remaining() != AesCtrHmacStreaming.this.getHeaderLength()) {
                throw new InvalidAlgorithmParameterException("Invalid header length");
            }
            if (header.get() != AesCtrHmacStreaming.this.getHeaderLength()) {
                throw new GeneralSecurityException("Invalid ciphertext");
            }
            this.noncePrefix = new byte[7];
            byte[] bArr = new byte[AesCtrHmacStreaming.this.keySizeInBytes];
            header.get(bArr);
            header.get(this.noncePrefix);
            byte[] bArrDeriveKeyMaterial = AesCtrHmacStreaming.this.deriveKeyMaterial(bArr, aad);
            this.keySpec = AesCtrHmacStreaming.this.deriveKeySpec(bArrDeriveKeyMaterial);
            this.hmacKeySpec = AesCtrHmacStreaming.this.deriveHmacKeySpec(bArrDeriveKeyMaterial);
            this.cipher = AesCtrHmacStreaming.cipherInstance();
            this.mac = AesCtrHmacStreaming.this.macInstance();
        }

        @Override // com.google.crypto.tink.subtle.StreamSegmentDecrypter
        public synchronized void decryptSegment(ByteBuffer ciphertext, int segmentNr, boolean isLastSegment, ByteBuffer plaintext) throws GeneralSecurityException {
            int iPosition = ciphertext.position();
            byte[] bArrNonceForSegment = AesCtrHmacStreaming.this.nonceForSegment(this.noncePrefix, segmentNr, isLastSegment);
            int iRemaining = ciphertext.remaining();
            if (iRemaining >= AesCtrHmacStreaming.this.tagSizeInBytes) {
                int i = iPosition + (iRemaining - AesCtrHmacStreaming.this.tagSizeInBytes);
                ByteBuffer byteBufferDuplicate = ciphertext.duplicate();
                byteBufferDuplicate.limit(i);
                ByteBuffer byteBufferDuplicate2 = ciphertext.duplicate();
                byteBufferDuplicate2.position(i);
                this.mac.init(this.hmacKeySpec);
                this.mac.update(bArrNonceForSegment);
                this.mac.update(byteBufferDuplicate);
                byte[] bArrCopyOf = Arrays.copyOf(this.mac.doFinal(), AesCtrHmacStreaming.this.tagSizeInBytes);
                byte[] bArr = new byte[AesCtrHmacStreaming.this.tagSizeInBytes];
                byteBufferDuplicate2.get(bArr);
                if (!Bytes.equal(bArr, bArrCopyOf)) {
                    throw new GeneralSecurityException("Tag mismatch");
                }
                ciphertext.limit(i);
                this.cipher.init(1, this.keySpec, new IvParameterSpec(bArrNonceForSegment));
                this.cipher.doFinal(ciphertext, plaintext);
            } else {
                throw new GeneralSecurityException("Ciphertext too short");
            }
        }
    }
}
