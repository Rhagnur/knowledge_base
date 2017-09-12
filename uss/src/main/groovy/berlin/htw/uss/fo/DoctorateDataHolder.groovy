package berlin.htw.uss.fo

import java.sql.Timestamp

class DoctorateDataHolder {
    int id
    Timestamp created
    Timestamp lastmodified
    Timestamp accepted
    Timestamp finished
    String title
    String status
    String doctorandSN
    String doctorandGivenName
    String doctorandUniName
    Timestamp doctorandGradDate
    String universityName
    String universityCategory
    String universityLocation
    String mainFinancing
    String secondFinancing

    String debug() {
        """
id          = ${id}
Created     = $created
lastMod     = $lastmodified
accepted    = $accepted
finished    = $finished
title       = $title
status      = $status
doctSN      = $doctorandSN
doctGN      = $doctorandGivenName
doctUni     = $doctorandUniName
doctDate    = $doctorandGradDate
Uni         = $universityName
UniCat      = $universityCategory
UniPlace    = $universityLocation
mainFin     = $mainFinancing
secFin      = $secondFinancing
"""
    }

}
