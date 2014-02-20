package com.topsoft.search.icis.ecps;

/**
 * 注册资本等级 <br/>
 * LEVEL1-(10万元以下) <br/>
 * LEVEL2-(10万 ~ 100万) <br/>
 * LEVEL3-(100万 ~ 1000万) <br/>
 * LEVEL4-(1000万以上)
 * 
 * @author weichao
 * 
 */
public enum RegCapLevel {

	LEVEL1(1), LEVEL2(2), LEVEL3(3), LEVEL4(4);

	private int level;

	private RegCapLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return this.level;
	}

	public RegCapLevel valueOf(int level) {
		switch (level) {
		case 1: {
			return LEVEL1;
		}
		case 2: {
			return LEVEL2;
		}
		case 3: {
			return LEVEL3;
		}
		case 4: {
			return LEVEL4;
		}
		default: {
			return null;
		}
		}
	}
}
