import com.github.javafaker.Faker;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class Employee {
    String name;
    int age;
    double salary;
    int yearsOfExperience;
    String gender;
    String education;
    int hoursWorked;

    public Employee(String name, int age, double salary, int yearsOfExperience, String gender, String education, int hoursWorked) {
        this.name = name;
        this.age = age;
        this.salary = salary;
        this.yearsOfExperience = yearsOfExperience;
        this.gender = gender;
        this.education = education;
        this.hoursWorked = hoursWorked;
    }

    // Getters
    public double getSalary() {
        return salary;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public String getGender() {
        return gender;
    }

    public String getEducation() {
        return education;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public int getAge() {
        return age;
    }
}

public class EmployeeDataProcessor {
    private static final int NUMBER_OF_EMPLOYEES = 1000000;

    public static void main(String[] args) throws InterruptedException {
        List<Employee> employees = generateEmployees(NUMBER_OF_EMPLOYEES);
        int availableCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableCores);

        Future<Double> averageSalaryFuture = executorService.submit(() -> calculateAverageSalary(employees));
        Future<Long> experienceCountFuture = executorService.submit(() -> countWithExperienceOver(employees, 5));
        Future<Map<String, Double>> salaryExtremesFuture = executorService.submit(() -> findSalaryExtremes(employees));
        Future<Map<String, Long>> countByGenderFuture = executorService.submit(() -> countByGender(employees));
        Future<Map<String, Long>> countByEducationFuture = executorService.submit(() -> countByEducation(employees));
        Future<Long> totalHoursWorkedFuture = executorService.submit(() -> calculateTotalHoursWorked(employees));
        Future<Double> averageAgeFuture = executorService.submit(() -> calculateAverageAge(employees));

        // Очікування завершення всіх завдань і виведення результатів
        try {
            System.out.println("Average Salary: " + averageSalaryFuture.get());
            System.out.println("Employees with > 5 years of experience: " + experienceCountFuture.get());
            System.out.println("Salary Extremes: " + salaryExtremesFuture.get());
            System.out.println("Count by Gender: " + countByGenderFuture.get());
            System.out.println("Count by Education: " + countByEducationFuture.get());
            System.out.println("Total Hours Worked: " + totalHoursWorkedFuture.get());
            System.out.println("Average Age: " + averageAgeFuture.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    private static List<Employee> generateEmployees(int numberOfEmployees) {
        List<Employee> employees = new ArrayList<>();
        Faker faker = new Faker();

        for (int i = 0; i < numberOfEmployees; i++) {
            employees.add(new Employee(
                faker.name().fullName(),
                faker.number().numberBetween(18, 65),
                faker.number().randomDouble(2, 30000, 100000),
                faker.number().numberBetween(0, 40),
                faker.options().option("Male", "Female"),
                faker.educator().course(),
                faker.number().numberBetween(100, 2000)
            ));
        }

        return employees;
    }

    private static double calculateAverageSalary(List<Employee> employees) {
        return employees.parallelStream().mapToDouble(Employee::getSalary).average().orElse(0);
    }

    private static long countWithExperienceOver(List<Employee> employees, int years) {
        return employees.parallelStream().filter(e -> e.getYearsOfExperience() > years).count();
    }

    private static Map<String, Double> findSalaryExtremes(List<Employee> employees) {
        return Map.of(
            "Max", employees.parallelStream().mapToDouble(Employee::getSalary).max().orElse(0),
            "Min", employees.parallelStream().mapToDouble(Employee::getSalary).min().orElse(0)
        );
    }

    private static Map<String, Long> countByGender(List<Employee> employees) {
        return employees.parallelStream()
            .collect(Collectors.groupingBy(Employee::getGender, Collectors.counting()));
    }

    private static Map<String, Long> countByEducation(List<Employee> employees) {
        return employees.parallelStream()
            .collect(Collectors.groupingBy(Employee::getEducation, Collectors.counting()));
    }

    private static long calculateTotalHoursWorked(List<Employee> employees) {
        return employees.parallelStream()
            .mapToLong(Employee::getHoursWorked)
            .sum();
    }

    private static double calculateAverageAge(List<Employee> employees) {
        return employees.parallelStream()
            .mapToInt(Employee::getAge)
            .average()
            .orElse(0);
    }
}
