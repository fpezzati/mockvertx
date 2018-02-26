package edu.pezzati.mockvertx.model;

import java.io.Serializable;
import java.util.Objects;

public class Doc implements Serializable {

    private static final long serialVersionUID = -8309232971059010158L;
    private String name;
    private String preview;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getPreview() {
	return preview;
    }

    public void setPreview(String preview) {
	this.preview = preview;
    }

    @Override
    public int hashCode() {
	return Objects.hash(name, preview);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Doc other = (Doc) obj;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (preview == null) {
	    if (other.preview != null)
		return false;
	} else if (!preview.equals(other.preview))
	    return false;
	return true;
    }
}
