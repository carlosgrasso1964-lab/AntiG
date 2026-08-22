package com.genas.app.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {
    private val familyDao = db.familyDao()
    private val personDao = db.personDao()
    private val partnershipDao = db.partnershipDao()

    // Family
    fun observeFirstFamily(): Flow<List<Family>> = familyDao.getAllFamilies()

    suspend fun getFirstFamily(): Family? = familyDao.getFirstFamily()

    suspend fun createFamily(name: String): Long {
        return familyDao.insert(Family(name = name))
    }

    suspend fun updateFamilyName(id: Long, newName: String) {
        val family = familyDao.getFamilyById(id) ?: return
        familyDao.update(family.copy(name = newName))
    }

    // People
    fun observePeople(familyId: Long): Flow<List<Person>> = personDao.getPeopleByFamily(familyId)

    suspend fun getPeopleByFamily(familyId: Long): List<Person> = personDao.getPeopleByFamilyList(familyId)

    suspend fun getPersonById(id: Long): Person? = personDao.getPersonById(id)

    suspend fun addPerson(person: Person): Long = personDao.insert(person)

    suspend fun updatePerson(person: Person) = personDao.update(person)

    suspend fun deletePerson(person: Person) {
        partnershipDao.deleteByPerson(person.id)
        personDao.clearParentId(person.id)
        personDao.delete(person)
    }

    suspend fun deleteFamilyAndPeople(familyId: Long) {
        partnershipDao.deleteByFamily(familyId)
        personDao.deleteByFamily(familyId)
        val family = familyDao.getFamilyById(familyId)
        if (family != null) familyDao.delete(family)
    }

    // Partnerships
    suspend fun getPartnerships(familyId: Long): List<Partnership> =
        partnershipDao.getByFamily(familyId)

    suspend fun addPartnership(person1Id: Long, person2Id: Long) {
        val person = personDao.getPersonById(person1Id) ?: return
        partnershipDao.insert(Partnership(
            familyId = person.familyId,
            person1Id = person1Id,
            person2Id = person2Id
        ))
    }
}
