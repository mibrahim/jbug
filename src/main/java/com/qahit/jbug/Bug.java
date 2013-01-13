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
        UNCONFIRMED,
        NEW,
        ASSIGNED,
        REOPENED,
        READY,
        IN_PROGRESS,
        PAUSED,
        RESOLVED,
        VERIFIED,
    }
    
    public enum Resolution
    {
        FIXED,
        WONT_FIX,
        WORKS_FOR_ME,
        WORKS_AS_DESIGNED
    }

    public enum Priorities
    {
        P1,
        P2,
        P3,
        P4,
        P5
    }

    public enum Severities
    {
        BLOCKER,
        CRITICAL,
        MAJOR,
        MINOR,
        TRIVIAL
    }
}
