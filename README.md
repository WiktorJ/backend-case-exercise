# backend-case-exercise
Jeden thread czyta i wrzuca do blocking queue
potem watki pobieraja z niej i wrzucaja do concurrentHashMapy<traceId, Object> oraz do NavigableSet, pobieraja też z niego i sprawdzają czy nie ma orphanow 
jak przychodzi null to shedulujemy taska (z delay) ktory ma zbudować obiekt (json) i wrzucic go do kolejki
watek pobiera z kolejki i zapisuje do pliku