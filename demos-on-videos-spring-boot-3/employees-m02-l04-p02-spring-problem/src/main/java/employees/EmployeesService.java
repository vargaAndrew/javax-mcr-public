package employees;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class EmployeesService {

    private ModelMapper modelMapper;

    private AtomicLong idGenerator = new AtomicLong();

    private List<Employee> employees = Collections.synchronizedList(new ArrayList<>(List.of(
        new Employee(idGenerator.incrementAndGet(), "John Doe"),
        new Employee(idGenerator.incrementAndGet(), "Jack Doe")
    )));

    public EmployeesService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public List<EmployeeDto> listEmployees(Optional<String> prefix) {
        Type targetListType = new TypeToken<List<EmployeeDto>>(){}.getType();
        List<Employee> filtered = employees.stream()
                .filter(e -> prefix.isEmpty() || e.getName().toLowerCase().startsWith(prefix.get().toLowerCase()))
                .collect(Collectors.toList());
        return modelMapper.map(filtered, targetListType);
    }

    public EmployeeDto findEmployeeById(long id) {
        return modelMapper.map(employees.stream()
                        .filter(e -> e.getId() == id).findAny()
                        .orElseThrow(notFoundException(id)),
                EmployeeDto.class);
    }

    public EmployeeDto createEmployee(CreateEmployeeCommand command) {
        Employee employee = new Employee(idGenerator.incrementAndGet(), command.getName());
        employees.add(employee);
        return modelMapper.map(employee, EmployeeDto.class);
    }

    public EmployeeDto updateEmployee(long id, UpdateEmployeeCommand command) {
        Employee employee = employees.stream()
                .filter(e -> e.getId() == id)
                .findFirst().orElseThrow(notFoundException(id));
        employee.setName(command.getName());
        return modelMapper.map(employee, EmployeeDto.class);
    }

    public void deleteEmployee(long id) {
        Employee employee = employees.stream()
                .filter(e -> e.getId() == id)
                .findFirst().orElseThrow(notFoundException(id));
        employees.remove(employee);
    }

    private static Supplier<EmployeeNotFoundException> notFoundException(long id) {
        return () -> new EmployeeNotFoundException("Employee not found: %d".formatted(id));
    }
}
