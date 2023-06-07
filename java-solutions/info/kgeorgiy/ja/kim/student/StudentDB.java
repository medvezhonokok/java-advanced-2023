package info.kgeorgiy.ja.kim.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {
    private final Comparator<Student> STUDENT_COMPARATOR = Comparator.
            comparing(Student::getLastName).
            thenComparing(Student::getFirstName).reversed().
            thenComparing(Student::compareTo);

    private <T> List<Student> findByKey(Collection<Student> students, Function<Student, T> function, T key) {
        return students.stream().
                filter(student -> function.apply(student).equals(key)).
                sorted(STUDENT_COMPARATOR).collect(Collectors.toList());
    }

    private List<Student> sortByComparator(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().
                sorted(comparator).
                collect(Collectors.toList());
    }

    private static <T> Stream<T> map(List<Student> students, Function<Student, T> function) {
        return students.stream().map(function);
    }

    private <T> List<T> getByFunction(List<Student> students, Function<Student, T> function) {
        return map(students, function).collect(Collectors.toCollection(ArrayList::new));
    }

    private <T> TreeSet<T> getDistinctByFunction(List<Student> students, Function<Student, T> function) {
        return map(students, function).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getByFunction(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getByFunction(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getByFunction(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getByFunction(students, st -> st.getFirstName() + " " + st.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getDistinctByFunction(students, Student::getFirstName);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortByComparator(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortByComparator(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findByKey(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findByKey(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findByKey(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream().
                collect(Collectors.toMap(
                        Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }
}
