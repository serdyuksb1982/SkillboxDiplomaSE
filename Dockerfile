FROM openjdk
ADD target/SkillboxDiplomaSE-1.0-SNAPSHOT.jar SkillboxDiplomaSE-1.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar", "SkillboxDiplomaSE-1.0-SNAPSHOT.jar"]
