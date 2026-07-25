with open('app/src/main/java/com/example/util/ComplexEvaluator.kt', 'r') as f:
    content = f.read()

# Let's see how parseLiteral is implemented
start_idx = content.find('fun parseLiteral(s: String): Complex {')
end_idx = content.find('fun formatComplex(c: Complex): String {')
print(content[start_idx:end_idx])
