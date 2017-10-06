/**
 * SqlColumnType.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

/**
 * Enumerator for SQL column types to make your life slightly easier
 * probably not worth the lines of code, but oh well, made it anyway.
 *
 * @author Jonodonozym
 */
public enum SqlColumnType{
	INT_1_BYTE,
	INT_2_BYTE,
	INT_3_BYTE,
	INT_4_BYTE,
	INT_8_BYTE,
	INT,
	DOUBLE,
	STRING_32,
	STRING_64,
	STRING_128,
	STRING_256,
	STRING_512,
	STRING_1024,
	STRING_2048;
	
	@SuppressWarnings("incomplete-switch")
	String getSqlSyntax(){
		switch(this){
			case INT_1_BYTE: return "tinyint";
			case INT_2_BYTE: return "smallint";
			case INT_3_BYTE: return "mediumint";
			case INT_4_BYTE:
			case INT: return "int";
			case INT_8_BYTE: return "bigint";
			case DOUBLE: return "double";
		}
		
		return "varchar("+name().substring(name().indexOf("_")).replace("_", "")+")";
	}
}
