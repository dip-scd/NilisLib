package org.nilis.utils.data;


public class DataPair<TTag, TData> {
	private TTag tag;
	public TTag getTag() {
		return tag;
	}
	public void setTag(TTag tag) {
		this.tag = tag;
	}
	public TData getData() {
		return data;
	}
	public void setData(TData data) {
		this.data = data;
	}
	private TData data;
	public DataPair(TTag tag, TData data) {
		this.tag = tag;
		this.data = data;
	}
	
	public DataPair() {
		this.tag = null;
		this.data = null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataPair<?, ?> other = (DataPair<?, ?>) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
}
