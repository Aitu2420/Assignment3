import java.sql.*;
import java.util.*;

public class ACMPFILE {
    // Абстрактный класс Animal
    abstract static class Animal {
        private String name;
        private String diet;
        private int age;

        public Animal(String name, String diet, int age) {
            this.name = name;
            this.diet = diet;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDiet() {
            return diet;
        }

        public void setDiet(String diet) {
            this.diet = diet;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public abstract void makeSound(); // Абстрактный метод и полиморфизм

        @Override
        public String toString() {
            return "Animal{name='" + name + "', diet='" + diet + "', age=" + age + "}";
        }
    }

    // Конкретные классы животных
    static class Lion extends Animal {
        public Lion(String name, int age) {
            super(name, "Meat", age);
        }

        @Override
        public void makeSound() {
            System.out.println("Roar!");
        }
    }

    static class Elephant extends Animal {
        public Elephant(String name, int age) {
            super(name, "Plants", age);
        }

        @Override
        public void makeSound() {
            System.out.println("Trumpet!");
        }
    }

    static class Zebra extends Animal {
        public Zebra(String name, int age) {
            super(name, "Plants", age);
        }

        @Override
        public void makeSound() {
            System.out.println("Neigh!");
        }
    }

    // Класс для подключения к базе данных
    static class DatabaseConnection {
        private static final String URL = "jdbc:postgresql://localhost:5432/zoo_db"; // URL базы
        private static final String USER = "postgres"; // Имя пользователя
        private static final String PASSWORD = "michigun228"; // Пароль

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // Класс для работы с таблицей Animal
    static class AnimalRepository {
        public void saveAnimal(Animal animal) {
            String sql = "INSERT INTO Animal (name, diet, age, type) VALUES (?, ?, ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, animal.getName());
                statement.setString(2, animal.getDiet());
                statement.setInt(3, animal.getAge());
                statement.setString(4, animal.getClass().getSimpleName());
                statement.executeUpdate();
                System.out.println("Animal saved: " + animal.getName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<Animal> findAllAnimals() {
            String sql = "SELECT * FROM Animal";
            List<Animal> animals = new ArrayList<>();

            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String diet = resultSet.getString("diet");
                    int age = resultSet.getInt("age");
                    String type = resultSet.getString("type");

                    // Заменённый блок switch на if-else
                    Animal animal = null;
                    if ("Lion".equals(type)) {
                        animal = new Lion(name, age);
                    } else if ("Elephant".equals(type)) {
                        animal = new Elephant(name, age);
                    } else if ("Zebra".equals(type)) {
                        animal = new Zebra(name, age);
                    } else {
                        throw new IllegalStateException("Unknown animal type: " + type);
                    }

                    animals.add(animal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return animals;
        }

        public void updateAnimalAge(String name, int newAge) {
            String sql = "UPDATE Animal SET age = ? WHERE name = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, newAge);
                statement.setString(2, name);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Animal updated: " + name + ", new age: " + newAge);
                } else {
                    System.out.println("Animal not found: " + name);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void deleteAnimal(String name) {
            String sql = "DELETE FROM Animal WHERE name = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Animal deleted: " + name);
                } else {
                    System.out.println("Animal not found: " + name);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Главный метод
    public static void main(String[] args) {
        AnimalRepository repository = new AnimalRepository();

        // Создаем животных
        Animal zebra = new Zebra("Ziggy", 3);
        Animal lion = new Lion("Simba", 5);
        Animal elephant = new Elephant("Dumbo", 10);

        // Сохраняем животных в базе данных
        repository.saveAnimal(zebra);
        repository.saveAnimal(lion);
        repository.saveAnimal(elephant);

        // Читаем всех животных из базы
        System.out.println("Animals in database:");
        repository.findAllAnimals().forEach(System.out::println);

        // Обновляем возраст животного
        repository.updateAnimalAge("Simba", 6);

        // Удаляем животное
        repository.deleteAnimal("Dumbo");

        // Читаем животных после изменений
        System.out.println("Animals in database after updates:");
        repository.findAllAnimals().forEach(System.out::println);
    }
}

