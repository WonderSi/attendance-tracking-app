package org.example.server;

import java.util.Date;

public record getData (Integer studentID, Date date, String discipline, String grouping, String firstName, String lastName, String status, String note) {
}