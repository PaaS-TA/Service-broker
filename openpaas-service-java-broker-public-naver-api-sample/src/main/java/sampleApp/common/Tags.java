package sampleApp.common;


import java.util.Arrays;
import java.util.List;

/**
 * Catalog에서 제공하는 TAG 정보
 * 
 * @author 안찬영
 *
 * History
 * 2015.7.9 최초개발
 */
public class Tags {
	private String[] values;

	public Tags(String... values) {
		this.values = values;
	}

	public String[] getTags() {
		return values;
	}

	public boolean containsOne(List<String> tags) {
		if (tags != null) {
			for (String value : values) {
				if (tags.contains(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean contains(String tag) {
		return tag != null && Arrays.asList(values).contains(tag);
	}

	public boolean startsWith(String tag) {
		if (tag != null) {
			for (String value : values) {
				if (tag.startsWith(value)) {
					return true;
				}
			}
		}
		return false;
	}
}