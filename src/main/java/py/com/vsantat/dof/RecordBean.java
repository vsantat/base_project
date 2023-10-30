package py.com.vsantat.dof;


import java.io.Serializable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class RecordBean implements Serializable {

	private static final long serialVersionUID = -6701257812344026697L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RecordBean other = (RecordBean) obj;
		if (isNew()) {
			return super.equals(obj);
		} else {
			if (id != other.id) {
				return false;
			}
		}
		return true;
	}

	public boolean isNew() {
		if (this.id <= 0) {
			return true;
		}
		return false;
	}
}