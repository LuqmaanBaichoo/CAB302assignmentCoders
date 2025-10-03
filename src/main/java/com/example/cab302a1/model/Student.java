package com.example.cab302a1.model;

import java.sql.Timestamp;

public class Student extends User{

    public Student(int _user_id, String _username, String _email, String _role, Timestamp _created_at){
       super(_user_id, _username, _email, _role, _created_at);
    }
}
