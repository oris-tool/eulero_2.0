/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.modeling.deprecated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DAGEdge")
@XmlAccessorType(XmlAccessType.FIELD)
public class DAGEdge {

    private String pre;
    private String post;

    @XmlElement(name = "pre", required = true)
    private Integer preInt;

    @XmlElement(name = "post", required = true)
    private Integer postInt;

    public DAGEdge(){}

    public DAGEdge(String pre, String post){
        this.pre = pre;
        this.post = post;
    }

    public DAGEdge(Integer preInt, Integer postInt){
        this.preInt = preInt;
        this.postInt = postInt;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public Integer getPreInt() {
        return preInt;
    }

    public Integer getPostInt() {
        return postInt;
    }

    public static DAGEdge of(String pre, String post){
        return new DAGEdge(pre, post);
    }

    public static DAGEdge of(Integer pre, Integer post){
        return new DAGEdge(pre, post);
    }
}
