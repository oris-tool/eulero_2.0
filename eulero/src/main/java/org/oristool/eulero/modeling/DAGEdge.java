package org.oristool.eulero.modeling;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DAGEdge")
@XmlAccessorType(XmlAccessType.FIELD)
public class DAGEdge {

    @XmlElement(name = "pre", required = true)
    private String pre;

    @XmlElement(name = "post", required = true)
    private String post;

    public DAGEdge(){}

    public DAGEdge(String pre, String post){
        this.pre = pre;
        this.post = post;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public static DAGEdge of(String pre, String post){
        return new DAGEdge(pre, post);
    }
}
