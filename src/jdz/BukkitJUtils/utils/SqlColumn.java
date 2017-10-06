/**
 * SqlColumn.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

/**
 * Represents a column in an sql table
 *
 * @author Jaiden Baker
 */
public final class SqlColumn{
	private final String name;
	private final SqlColumnType type;
	
	public SqlColumn(String name, SqlColumnType type){
		this.name = name;
		this.type = type;
	}
	
	public String name(){
		return name;
	}
	
	public SqlColumnType type(){
		return type;
	}
}
