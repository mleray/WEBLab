package Beerculator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
@ManagedBean(name="timeBean")
@RequestScoped

public class TimeBean {
	public String getTime() {
		return new java.util.Date().toString();
		}
}
