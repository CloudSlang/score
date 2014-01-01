package com.hp.oo.internal.sdk.execution;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ronen
 * Date: 24/04/12
 */
public class ExecutionSystemAccount implements Serializable {

    private static final long serialVersionUID = 2453159875486388127L;

    private String name;
    private String username;
    private String password;

	private ExecutionSystemAccount(){

	}

    public ExecutionSystemAccount(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ExecutionSystemAccount{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + "******" + '\'' +
                '}';
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ExecutionSystemAccount)) return false;

		ExecutionSystemAccount that = (ExecutionSystemAccount) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (password != null ? !password.equals(that.password) : that.password != null) return false;
		if (username != null ? !username.equals(that.username) : that.username != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}
}
