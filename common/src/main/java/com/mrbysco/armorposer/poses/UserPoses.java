package com.mrbysco.armorposer.poses;

import com.mrbysco.armorposer.util.PoseData;

import java.util.List;
import java.util.Objects;

public class UserPoses {
	private final List<PoseData> poses;

	public UserPoses(List<PoseData> initialList) {
		this.poses = initialList;
	}

	public List<PoseData> userPoses() {
		return poses;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (UserPoses) obj;
		return Objects.equals(this.poses, that.poses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(poses);
	}

	@Override
	public String toString() {
		return "UserPoses[" +
				"poses=" + poses + ']';
	}
}
