package cn.flyrise.feep.protocol;

import cn.flyrise.feep.core.services.IRsaService;

public class FeepRsaService implements IRsaService {

	private String publicKey;

	public FeepRsaService(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String getPublicKey() {
		return publicKey;
	}
}
