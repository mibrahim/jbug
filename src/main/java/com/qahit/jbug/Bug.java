/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

/**
 *
 * @author mosama
 */
public class Bug
{
    public enum Status
    {
        OPEN,
        IN_PROGRESS,
        PAUSED,
        CLOSED
    }
    
    public enum Easiness
    {
	EASY,
	MEDIUM,
	HARD
    }
    
    public enum Priorities
    {
        HIGH,
        MEDIUM,
        LOW
    }
}
