package cn.flyrise.feep.media.images.bean;

/**
 * @author ZYP
 * @since 2017-10-18 15:00
 */
public class Album {

	public String id;
	public String name;
	public String cover;   // 封面路径，一般是第一个...
	public int count;

	public Album(String id, String name, String cover, int count) {
		this.id = id;
		this.name = name;
		this.cover = cover;
		this.count = count;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Album that = (Album) o;

		if (!id.equals(that.id)) return false;
		return name.equals(that.name);

	}

	@Override public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}
}
