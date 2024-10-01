package com.mrbysco.armorposer.util;

import java.util.Objects;

public final class PoseData {
	private String name;
	private String data;

	public PoseData(String name, String data) {
		this.name = name;
		this.data = data;
	}

	public String name() {
		return name;
	}

	public String data() {
		return data;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (PoseData) obj;
		return Objects.equals(this.name, that.name) &&
				Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, data);
	}

	@Override
	public String toString() {
		return "PoseData[" +
				"name=" + name + ", " +
				"data=" + data + ']';
	}

}
