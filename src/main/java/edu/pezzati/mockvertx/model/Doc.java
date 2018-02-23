package edu.pezzati.mockvertx.model;

import java.io.Serializable;

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
}
