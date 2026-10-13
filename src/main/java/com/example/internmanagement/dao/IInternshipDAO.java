package com.example.internmanagement.dao;

import com.example.internmanagement.model.InternProfile;

public interface IInternshipDAO {

    InternProfile getInternshipByInternId(String internId);
}