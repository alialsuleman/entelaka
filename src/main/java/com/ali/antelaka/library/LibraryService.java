package com.ali.antelaka.library;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibraryService {


    public Map<String, List<CodeExampleDTO>> getAllCodeExamples() {
        Map<String, List<CodeExampleDTO>> examples = new HashMap<>();
        examples.put("cpp", getCppExamples());
        examples.put("java", getJavaExamples());
        examples.put("python", getPythonExamples());
        examples.put("javascript", getJavaScriptExamples());
        return examples;
    }

    public List<CodeExampleDTO> getCodeExamples(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return getJavaExamples();
            case "python":
                return getPythonExamples();
            case "javascript":
                return getJavaScriptExamples();
            case "cpp":
            case "c++":
                return getCppExamples();
            default:
                return getCppExamples(); // default fallback
        }
    }

    private List<CodeExampleDTO> getJavaExamples() {
        return List.of(
                new CodeExampleDTO(
                        "Java",
                        "Addition of Two Numbers",
                        "Simple program to add two integers",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int a = scanner.nextInt();
                                int b = scanner.nextInt();
                                System.out.println(a + b);
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Multiplication of Two Numbers",
                        "Program to multiply two integers",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int a = scanner.nextInt();
                                int b = scanner.nextInt();
                                System.out.println(a * b);
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Greatest Common Divisor (GCD)",
                        "Find GCD using Euclidean Algorithm",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static int gcd(int a, int b) {
                                while (b != 0) {
                                    int r = a % b;
                                    a = b;
                                    b = r;
                                }
                                return a;
                            }
                            
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int a = scanner.nextInt();
                                int b = scanner.nextInt();
                                System.out.println(gcd(a, b));
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Least Common Multiple (LCM)",
                        "Find LCM using GCD",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static int gcd(int a, int b) {
                                return b == 0 ? a : gcd(b, a % b);
                            }
                            
                            public static int lcm(int a, int b) {
                                return (a / gcd(a, b)) * b;
                            }
                            
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int a = scanner.nextInt();
                                int b = scanner.nextInt();
                                System.out.println(lcm(a, b));
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Factorial",
                        "Calculate factorial of a number",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int n = scanner.nextInt();
                                long fact = 1;
                                
                                for (int i = 1; i <= n; i++) {
                                    fact *= i;
                                }
                                
                                System.out.println(fact);
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Prime Number Check",
                        "Check whether a number is prime",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int n = scanner.nextInt();
                                boolean prime = n > 1;
                                
                                for (int i = 2; i * i <= n; i++) {
                                    if (n % i == 0) {
                                        prime = false;
                                        break;
                                    }
                                }
                                
                                System.out.println(prime ? "Prime" : "Not Prime");
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Swap Two Numbers",
                        "Swap two numbers using temporary variable",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int a = scanner.nextInt();
                                int b = scanner.nextInt();
                                
                                // Swap using temporary variable
                                int temp = a;
                                a = b;
                                b = temp;
                                
                                System.out.println(a + " " + b);
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Fibonacci Series",
                        "Print Fibonacci sequence",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int n = scanner.nextInt();
                                
                                int a = 0, b = 1;
                                for (int i = 1; i <= n; i++) {
                                    System.out.print(a + " ");
                                    int c = a + b;
                                    a = b;
                                    b = c;
                                }
                                
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Sum of Array",
                        "Calculate sum of array elements",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int n = scanner.nextInt();
                                int[] arr = new int[n];
                                int sum = 0;
                                
                                // Read array elements
                                for (int i = 0; i < n; i++) {
                                    arr[i] = scanner.nextInt();
                                }
                                
                                // Calculate sum
                                for (int i = 0; i < n; i++) {
                                    sum += arr[i];
                                }
                                
                                System.out.println(sum);
                                scanner.close();
                            }
                        }
                        """
                ),

                new CodeExampleDTO(
                        "Java",
                        "Reverse a Number",
                        "Reverse digits of a number",
                        """
                        import java.util.Scanner;
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int n = scanner.nextInt();
                                int rev = 0;
                                
                                while (n > 0) {
                                    rev = rev * 10 + n % 10;
                                    n /= 10;
                                }
                                
                                System.out.println(rev);
                                scanner.close();
                            }
                        }
                        """
                ),

                // مثال إضافي يظهر مميزات Java
                new CodeExampleDTO(
                        "Java",
                        "Object-Oriented Programming Example",
                        "Simple class and object example",
                        """
                        import java.util.Scanner;
                        
                        class Calculator {
                            // Method to add two numbers
                            public int add(int a, int b) {
                                return a + b;
                            }
                            
                            // Method to multiply two numbers
                            public int multiply(int a, int b) {
                                return a * b;
                            }
                        }
                        
                        public class Main {
                            public static void main(String[] args) {
                                Scanner scanner = new Scanner(System.in);
                                int x = scanner.nextInt();
                                int y = scanner.nextInt();
                                
                                // Create object of Calculator class
                                Calculator calc = new Calculator();
                                
                                System.out.println("Sum: " + calc.add(x, y));
                                System.out.println("Product: " + calc.multiply(x, y));
                                
                                scanner.close();
                            }
                        }
                        """
                )
        );
    }

    private List<CodeExampleDTO> getCppExamples() {
        return List.of(
                new CodeExampleDTO(
                        "C++",
                        "Addition of Two Numbers",
                        "Simple program to add two integers",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int a, b;
                            cin >> a >> b;
                            cout << a + b;
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Multiplication of Two Numbers",
                        "Program to multiply two integers",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int a, b;
                            cin >> a >> b;
                            cout << a * b;
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Greatest Common Divisor (GCD)",
                        "Find GCD using Euclidean Algorithm",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int gcd(int a, int b) {
                            while (b != 0) {
                                int r = a % b;
                                a = b;
                                b = r;
                            }
                            return a;
                        }
            
                        int main() {
                            int a, b;
                            cin >> a >> b;
                            cout << gcd(a, b);
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Least Common Multiple (LCM)",
                        "Find LCM using GCD",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int gcd(int a, int b) {
                            return b == 0 ? a : gcd(b, a % b);
                        }
            
                        int lcm(int a, int b) {
                            return (a / gcd(a, b)) * b;
                        }
            
                        int main() {
                            int a, b;
                            cin >> a >> b;
                            cout << lcm(a, b);
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Factorial",
                        "Calculate factorial of a number",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int n;
                            long long fact = 1;
                            cin >> n;
            
                            for (int i = 1; i <= n; i++)
                                fact *= i;
            
                            cout << fact;
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Prime Number Check",
                        "Check whether a number is prime",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int n;
                            cin >> n;
            
                            bool prime = n > 1;
                            for (int i = 2; i * i <= n; i++) {
                                if (n % i == 0) {
                                    prime = false;
                                    break;
                                }
                            }
            
                            cout << (prime ? "Prime" : "Not Prime");
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Swap Two Numbers",
                        "Swap two numbers using temporary variable",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int a, b, temp;
                            cin >> a >> b;
            
                            temp = a;
                            a = b;
                            b = temp;
            
                            cout << a << " " << b;
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Fibonacci Series",
                        "Print Fibonacci sequence",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int n;
                            cin >> n;
            
                            int a = 0, b = 1;
                            for (int i = 1; i <= n; i++) {
                                cout << a << " ";
                                int c = a + b;
                                a = b;
                                b = c;
                            }
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Sum of Array",
                        "Calculate sum of array elements",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int n, sum = 0;
                            cin >> n;
                            int arr[n];
            
                            for (int i = 0; i < n; i++)
                                cin >> arr[i];
            
                            for (int i = 0; i < n; i++)
                                sum += arr[i];
            
                            cout << sum;
                            return 0;
                        }
                        """
                ),

                new CodeExampleDTO(
                        "C++",
                        "Reverse a Number",
                        "Reverse digits of a number",
                        """
                        #include <iostream>
                        using namespace std;
            
                        int main() {
                            int n, rev = 0;
                            cin >> n;
            
                            while (n > 0) {
                                rev = rev * 10 + n % 10;
                                n /= 10;
                            }
            
                            cout << rev;
                            return 0;
                        }
                        """
                ),

                // مثال إضافي يظهر مميزات C++
                new CodeExampleDTO(
                        "C++",
                        "Function Overloading",
                        "Example of function overloading in C++",
                        """
                        #include <iostream>
                        using namespace std;
            
                        // Function to add two integers
                        int add(int a, int b) {
                            return a + b;
                        }
            
                        // Function to add three integers (overloaded)
                        int add(int a, int b, int c) {
                            return a + b + c;
                        }
            
                        // Function to add two doubles (overloaded)
                        double add(double a, double b) {
                            return a + b;
                        }
            
                        int main() {
                            cout << "Sum of 5 and 10: " << add(5, 10) << endl;
                            cout << "Sum of 5, 10 and 15: " << add(5, 10, 15) << endl;
                            cout << "Sum of 5.5 and 10.5: " << add(5.5, 10.5) << endl;
                            
                            return 0;
                        }
                        """
                ),

                // مثال آخر: استخدام الـ STL
                new CodeExampleDTO(
                        "C++",
                        "STL Vector Example",
                        "Using C++ Standard Template Library (STL)",
                        """
                        #include <iostream>
                        #include <vector>
                        #include <algorithm> // for sort
                        using namespace std;
            
                        int main() {
                            vector<int> numbers = {5, 2, 8, 1, 9, 3};
                            
                            cout << "Original vector: ";
                            for (int num : numbers) {
                                cout << num << " ";
                            }
                            cout << endl;
                            
                            // Sort the vector
                            sort(numbers.begin(), numbers.end());
                            
                            cout << "Sorted vector: ";
                            for (int num : numbers) {
                                cout << num << " ";
                            }
                            cout << endl;
                            
                            // Find sum using range-based for loop
                            int sum = 0;
                            for (int num : numbers) {
                                sum += num;
                            }
                            cout << "Sum of elements: " << sum << endl;
                            
                            return 0;
                        }
                        """
                )
        );
    }

    private List<CodeExampleDTO> getPythonExamples() {
        return List.of(
                new CodeExampleDTO(
                        "Python",
                        "Addition of Two Numbers",
                        "Simple program to add two integers",
                        """
                        # Method 1: Using input() function
                        a = int(input("Enter first number: "))
                        b = int(input("Enter second number: "))
                        print(f"Sum: {a + b}")
                        
                        # Method 2: Single line input
                        # a, b = map(int, input().split())
                        # print(a + b)
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Multiplication of Two Numbers",
                        "Program to multiply two integers",
                        """
                        a = int(input("Enter first number: "))
                        b = int(input("Enter second number: "))
                        print(f"Product: {a * b}")
                        
                        # Using lambda function
                        # multiply = lambda x, y: x * y
                        # print(multiply(a, b))
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Greatest Common Divisor (GCD)",
                        "Find GCD using math module",
                        """
                        import math
                        
                        a = int(input("Enter first number: "))
                        b = int(input("Enter second number: "))
                        
                        # Using math.gcd() function
                        gcd_result = math.gcd(a, b)
                        print(f"GCD: {gcd_result}")
                        
                        # Manual implementation
                        # def gcd_manual(x, y):
                        #     while y:
                        #         x, y = y, x % y
                        #     return x
                        # print(gcd_manual(a, b))
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Least Common Multiple (LCM)",
                        "Find LCM using math module",
                        """
                        import math
                        
                        a = int(input("Enter first number: "))
                        b = int(input("Enter second number: "))
                        
                        # Using math.lcm() function (Python 3.9+)
                        lcm_result = math.lcm(a, b)
                        print(f"LCM: {lcm_result}")
                        
                        # Manual implementation using GCD
                        # def lcm_manual(x, y):
                        #     return abs(x * y) // math.gcd(x, y)
                        # print(lcm_manual(a, b))
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Factorial",
                        "Calculate factorial of a number",
                        """
                        import math
                        
                        n = int(input("Enter a number: "))
                        
                        # Method 1: Using math.factorial()
                        fact = math.factorial(n)
                        print(f"Factorial using math module: {fact}")
                        
                        # Method 2: Manual calculation
                        # fact_manual = 1
                        # for i in range(1, n + 1):
                        #     fact_manual *= i
                        # print(f"Manual factorial: {fact_manual}")
                        
                        # Method 3: Using recursion
                        # def factorial_recursive(x):
                        #     return 1 if x <= 1 else x * factorial_recursive(x - 1)
                        # print(f"Recursive factorial: {factorial_recursive(n)}")
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Prime Number Check",
                        "Check whether a number is prime",
                        """
                        def is_prime(n):
                            if n <= 1:
                                return False
                            if n <= 3:
                                return True
                            if n % 2 == 0 or n % 3 == 0:
                                return False
                            
                            i = 5
                            while i * i <= n:
                                if n % i == 0 or n % (i + 2) == 0:
                                    return False
                                i += 6
                            return True
                        
                        num = int(input("Enter a number: "))
                        
                        if is_prime(num):
                            print(f"{num} is a prime number")
                        else:
                            print(f"{num} is not a prime number")
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Swap Two Numbers",
                        "Swap two numbers in Python way",
                        """
                        # Pythonic way to swap
                        a = int(input("Enter first number: "))
                        b = int(input("Enter second number: "))
                        
                        print(f"Before swap: a = {a}, b = {b}")
                        
                        # Method 1: Using tuple unpacking
                        a, b = b, a
                        
                        # Method 2: Using arithmetic
                        # a = a + b
                        # b = a - b
                        # a = a - b
                        
                        # Method 3: Using XOR
                        # a = a ^ b
                        # b = a ^ b
                        # a = a ^ b
                        
                        print(f"After swap: a = {a}, b = {b}")
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "Fibonacci Series",
                        "Print Fibonacci sequence",
                        """
                        def fibonacci(n):
                            
                a, b = 0, 1
                series = []
                for _ in range(n):
                    series.append(a)
                    a, b = b, a + b
                return series
            
                n = int(input("Enter number of terms: "))
                
                # Method 1: Using function
                fib_series = fibonacci(n)
                print(f"Fibonacci series: {fib_series}")
                
                # Method 2: Generator version
                # def fib_generator(n):
                #     a, b = 0, 1
                #     for _ in range(n):
                #         yield a
                #         a, b = b, a + b
                
                # print(list(fib_generator(n)))
                """
        ),
                new CodeExampleDTO(
                        "Python",
                        "Reverse a Number",
                        "Reverse digits of a number",
                        """
                        def reverse_number(n):
                            reverse = 0
                            original = n
                            
                            while n > 0:
                                digit = n % 10
                                reverse = reverse * 10 + digit
                                n //= 10
                            
                            return reverse
                        
                        num = int(input("Enter a number: "))
                        reversed_num = reverse_number(num)
                        print(f"Original: {num}")
                        print(f"Reversed: {reversed_num}")
                        
                        # Pythonic way for positive numbers
                        # reversed_pythonic = int(str(num)[::-1])
                        # print(f"Pythonic reversed: {reversed_pythonic}")
                        """
        ),

        new CodeExampleDTO(
                "Python",
                "Sum of List",
                "Calculate sum of list elements",
                """
                # Method 1: Using sum() function
                numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
                total = sum(numbers)
                print(f"Sum of list: {total}")
                
                # Method 2: User input list
                # n = int(input("Enter number of elements: "))
                # user_list = []
                # for i in range(n):
                #     user_list.append(int(input(f"Enter element {i+1}: ")))
                
                # print(f"User list: {user_list}")
                # print(f"Sum: {sum(user_list)}")
                
                # Method 3: Using reduce (from functools)
                # from functools import reduce
                # total_reduce = reduce(lambda x, y: x + y, numbers)
                # print(f"Sum using reduce: {total_reduce}")
                """
        ),


        // أمثلة إضافية تظهر مميزات Python
        new CodeExampleDTO(
                "Python",
                "List Comprehensions",
                "Pythonic way to create lists",
                """
                # Example 1: Squares of numbers
                squares = [x**2 for x in range(1, 11)]
                print(f"Squares: {squares}")
                
                # Example 2: Even numbers only
                evens = [x for x in range(1, 21) if x % 2 == 0]
                print(f"Even numbers: {evens}")
                
                # Example 3: Nested list comprehension
                matrix = [[i * j for j in range(1, 4)] for i in range(1, 4)]
                print(f"3x3 Matrix: {matrix}")
                
                # Example 4: List comprehension with condition
                numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
                filtered = [x for x in numbers if x > 5 and x % 2 == 0]
                print(f"Numbers > 5 and even: {filtered}")
                """
        ),

                new CodeExampleDTO(
                        "Python",
                        "Dictionary and Set Operations",
                        "Working with Python dictionaries and sets",
                        """
                        # Dictionary example
                        student = {
                            "name": "John Doe",
                            "age": 20,
                            "courses": ["Math", "Physics", "Computer Science"],
                            "grades": {"Math": 95, "Physics": 88, "CS": 92}
                        }
                        
                        print("Student Information:")
                        for key, value in student.items():
                            print(f"{key}: {value}")
                        
                        # Set operations
                        set_a = {1, 2, 3, 4, 5}
                        set_b = {4, 5, 6, 7, 8}
                        
                        print(f"\\nSet A: {set_a}")
                        print(f"Set B: {set_b}")
                        print(f"Union: {set_a.union(set_b)}")
                        print(f"Intersection: {set_a.intersection(set_b)}")
                        print(f"Difference (A - B): {set_a.difference(set_b)}")
                        print(f"Symmetric Difference: {set_a.symmetric_difference(set_b)}")
                        """
                ),

                new CodeExampleDTO(
                        "Python",
                        "File Operations",
                        "Reading and writing files in Python",
                        """
                        # Writing to a file
                        with open('example.txt', 'w') as file:
                            file.write("Hello, World!\\n")
                            file.write("This is a Python file handling example.\\n")
                            file.write("Line 3\\n")
                        
                        print("File written successfully!")
                        
                        # Reading from a file
                        print("\\nReading file contents:")
                        with open('example.txt', 'r') as file:
                            # Read all content
                            content = file.read()
                            print(content)
                        
                        # Reading line by line
                        print("\\nReading line by line:")
                        with open('example.txt', 'r') as file:
                            for line_number, line in enumerate(file, 1):
                                print(f"Line {line_number}: {line.strip()}")
                        
                        # Appending to a file
                        with open('example.txt', 'a') as file:
                            file.write("This line was appended.\\n")
                        
                        print("\\nFile updated with new content.")
                        """
                )
    );
    }


    private List<CodeExampleDTO> getJavaScriptExamples() {
        return List.of(
                new CodeExampleDTO(
                        "JavaScript",
                        "Addition of Two Numbers",
                        "Simple program to add two integers",
                        """
                        // Method 1: Using Node.js readline module
                        const readline = require('readline');
                        
                        const rl = readline.createInterface({
                            input: process.stdin,
                            output: process.stdout
                        });
                        
                        rl.question('Enter two numbers (separated by space): ', (input) => {
                            const [a, b] = input.split(' ').map(Number);
                            console.log(`Sum: ${a + b}`);
                            rl.close();
                        });
                        
                        // Method 2: Browser version
                        /*
                        <script>
                            function addNumbers() {
                                const a = parseInt(document.getElementById('num1').value);
                                const b = parseInt(document.getElementById('num2').value);
                                const result = a + b;
                                document.getElementById('result').innerText = `Sum: ${result}`;
                            }
                        </script>
                        
                        <input type="number" id="num1">
                        <input type="number" id="num2">
                        <button onclick="addNumbers()">Add</button>
                        <div id="result"></div>
                        */
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Multiplication of Two Numbers",
                        "Program to multiply two integers",
                        """
                        // Using arrow function
                        const multiply = (a, b) => a * b;
                        
                        // Example usage
                        const num1 = 5;
                        const num2 = 10;
                        console.log(`Product: ${multiply(num1, num2)}`);
                        
                        // Interactive version with prompt (browser)
                        /*
                        const a = parseFloat(prompt("Enter first number:"));
                        const b = parseFloat(prompt("Enter second number:"));
                        alert(`Product: ${a * b}`);
                        */
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Greatest Common Divisor (GCD)",
                        "Find GCD using Euclidean Algorithm",
                        """
                        // Function to find GCD
                        function gcd(a, b) {
                            while (b !== 0) {
                                const temp = b;
                                b = a % b;
                                a = temp;
                            }
                            return a;
                        }
                        
                        // Using recursion
                        const gcdRecursive = (a, b) => b === 0 ? a : gcdRecursive(b, a % b);
                        
                        // Example
                        const num1 = 48;
                        const num2 = 18;
                        console.log(`GCD of ${num1} and ${num2}:`);
                        console.log(`Iterative: ${gcd(num1, num2)}`);
                        console.log(`Recursive: ${gcdRecursive(num1, num2)}`);
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Least Common Multiple (LCM)",
                        "Find LCM using GCD",
                        """
                        // Function to find GCD (needed for LCM)
                        const gcd = (a, b) => b === 0 ? a : gcd(b, a % b);
                        
                        // Function to find LCM
                        const lcm = (a, b) => Math.abs(a * b) / gcd(a, b);
                        
                        // Example with multiple numbers
                        const lcmMultiple = (...numbers) => {
                            return numbers.reduce((acc, curr) => lcm(acc, curr));
                        };
                        
                        // Example usage
                        console.log(`LCM of 12 and 18: ${lcm(12, 18)}`);
                        console.log(`LCM of 4, 6, and 8: ${lcmMultiple(4, 6, 8)}`);
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Factorial",
                        "Calculate factorial of a number",
                        """
                        // Method 1: Using loop
                        function factorialLoop(n) {
                            let result = 1;
                            for (let i = 2; i <= n; i++) {
                                result *= i;
                            }
                            return result;
                        }
                        
                        // Method 2: Using recursion
                        function factorialRecursive(n) {
                            return n <= 1 ? 1 : n * factorialRecursive(n - 1);
                        }
                        
                        // Method 3: Using reduce
                        const factorialReduce = n => {
                            return Array.from({length: n}, (_, i) => i + 1)
                                       .reduce((acc, curr) => acc * curr, 1);
                        };
                        
                        // Example
                        const number = 5;
                        console.log(`Factorial of ${number}:`);
                        console.log(`Loop: ${factorialLoop(number)}`);
                        console.log(`Recursive: ${factorialRecursive(number)}`);
                        console.log(`Reduce: ${factorialReduce(number)}`);
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Prime Number Check",
                        "Check whether a number is prime",
                        """
                        function isPrime(n) {
                            if (n <= 1) return false;
                            if (n <= 3) return true;
                            if (n % 2 === 0 || n % 3 === 0) return false;
                            
                            for (let i = 5; i * i <= n; i += 6) {
                                if (n % i === 0 || n % (i + 2) === 0) {
                                    return false;
                                }
                            }
                            return true;
                        }
                        
                        // Optimized version with caching
                        const primeChecker = (() => {
                            const cache = new Map();
                            
                            return function(n) {
                                if (cache.has(n)) return cache.get(n);
                                
                                const result = isPrime(n);
                                cache.set(n, result);
                                return result;
                            };
                        })();
                        
                        // Example
                        const numbers = [2, 3, 4, 5, 17, 20, 29];
                        console.log("Prime number check:");
                        numbers.forEach(num => {
                            console.log(`${num}: ${primeChecker(num) ? 'Prime' : 'Not Prime'}`);
                        });
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Swap Two Numbers",
                        "Swap two numbers in JavaScript",
                        """
                        // Method 1: Using temporary variable
                        let a = 5;
                        let b = 10;
                        console.log(`Before swap: a = ${a}, b = ${b}`);
                        
                        let temp = a;
                        a = b;
                        b = temp;
                        console.log(`After swap (temp): a = ${a}, b = ${b}`);
                        
                        // Method 2: Using destructuring (ES6)
                        [a, b] = [b, a];
                        console.log(`After destructuring swap: a = ${a}, b = ${b}`);
                        
                        // Method 3: Using arithmetic
                        a = a + b;
                        b = a - b;
                        a = a - b;
                        console.log(`After arithmetic swap: a = ${a}, b = ${b}`);
                        
                        // Method 4: Using XOR
                        a = a ^ b;
                        b = a ^ b;
                        a = a ^ b;
                        console.log(`After XOR swap: a = ${a}, b = ${b}`);
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Fibonacci Series",
                        "Print Fibonacci sequence",
                        """
                        // Method 1: Using loop
                        function fibonacciLoop(n) {
                            const sequence = [0, 1];
                            for (let i = 2; i < n; i++) {
                                sequence[i] = sequence[i - 1] + sequence[i - 2];
                            }
                            return sequence.slice(0, n);
                        }
                        
                        // Method 2: Using recursion (memoized for efficiency)
                        function fibonacciRecursive(n, memo = {}) {
                            if (n in memo) return memo[n];
                            if (n <= 1) return n;
                            
                            memo[n] = fibonacciRecursive(n - 1, memo) + 
                                      fibonacciRecursive(n - 2, memo);
                            return memo[n];
                        }
                        
                        // Method 3: Generator function (ES6)
                        function* fibonacciGenerator(n) {
                            let [a, b] = [0, 1];
                            for (let i = 0; i < n; i++) {
                                yield a;
                                [a, b] = [b, a + b];
                            }
                        }
                        
                        // Example
                        const n = 10;
                        console.log(`First ${n} Fibonacci numbers:`);
                        console.log(`Loop: ${fibonacciLoop(n)}`);
                        
                        const fibNumbers = Array.from({length: n}, (_, i) => fibonacciRecursive(i));
                        console.log(`Recursive: ${fibNumbers}`);
                        
                        const genResult = [...fibonacciGenerator(n)];
                        console.log(`Generator: ${genResult}`);
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Sum of Array",
                        "Calculate sum of array elements",
                        """
                        const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
                        
                        // Method 1: Using reduce
                        const sumReduce = numbers.reduce((acc, curr) => acc + curr, 0);
                        
                        // Method 2: Using for loop
                        let sumLoop = 0;
                        for (let i = 0; i < numbers.length; i++) {
                            sumLoop += numbers[i];
                        }
                        
                        // Method 3: Using for...of loop
                        let sumForOf = 0;
                        for (const num of numbers) {
                            sumForOf += num;
                        }
                        
                        // Method 4: Using forEach
                        let sumForEach = 0;
                        numbers.forEach(num => sumForEach += num);
                        
                        // Method 5: Using recursion
                        function sumRecursive(arr, index = 0) {
                            return index >= arr.length ? 0 : arr[index] + sumRecursive(arr, index + 1);
                        }
                        
                        console.log("Array:", numbers);
                        console.log("Sum using reduce:", sumReduce);
                        console.log("Sum using for loop:", sumLoop);
                        console.log("Sum using for...of:", sumForOf);
                        console.log("Sum using forEach:", sumForEach);
                        console.log("Sum using recursion:", sumRecursive(numbers));
                        """
                ),

                new CodeExampleDTO(
                        "JavaScript",
                        "Reverse a Number",
                        "Reverse digits of a number",
                        """
                        function reverseNumber(n) {
                            let reversed = 0;
                            let original = n;
                            
                            while (n > 0) {
                                const digit = n % 10;
                                reversed = reversed * 10 + digit;
                                n = Math.floor(n / 10);
                            }
                            
                            return reversed;
                        }
                        
                        // String method
                        const reverseStringMethod = n => {
                            return parseInt(n.toString().split('').reverse().join(''));
                        };
                        
                        // One-liner using arrow function and Math.sign for negative numbers
                        const reverseOneLiner = n => 
                            parseInt(Math.abs(n).toString().split('').reverse().join('')) * Math.sign(n);
                        
                        // Examples
                        const testNumbers = [12345, 100, 7, -123, 987654321];
                        
                        testNumbers.forEach(num => {
                            console.log(`Original: ${num}`);
                            console.log(`Reversed (math): ${reverseNumber(Math.abs(num)) * Math.sign(num)}`);
                            console.log(`Reversed (string): ${reverseStringMethod(Math.abs(num)) * Math.sign(num)}`);
                            console.log(`Reversed (one-liner): ${reverseOneLiner(num)}`);
                            console.log('---');
                        });
                        """
                )

        );
    }

}
