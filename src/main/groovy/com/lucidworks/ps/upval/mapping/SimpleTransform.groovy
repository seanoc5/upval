package com.lucidworks.ps.upval.mapping


/**
 * test class for working through dynamic function invokation
 */
class SimpleTransform {

    static String foo(bar){
        JsonReader reader = Json.createReader(new FileReader("books.json"));
        JsonStructure jsonStructure = reader.read();
        reader.close();


        return "In foo($bar)"


/*
        JsonObject json = Json.createObjectBuilder()
             .add("name", "Falco")
             .add("age", BigDecimal.valueOf(3))
             .add("biteable", Boolean.FALSE).build();
           String result = json.toString();
*/
    }

}
