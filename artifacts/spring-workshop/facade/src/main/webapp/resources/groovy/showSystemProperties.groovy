def sb=new StringBuilder()
for (def kvp: System.getProperties().entrySet()) {
    def propName=kvp.getKey()
    def propValue=kvp.getValue()
    sb.append(propName).append('=').append(propValue).append("\r\n")
}
sb.toString()
