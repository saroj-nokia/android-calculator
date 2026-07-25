import subprocess
print("Compiling EvalTest...")
subprocess.run(['javac', '-cp', 'app/src/main/java', 'app/src/test/java/com/example/EvalTest.kt'], capture_output=True)
