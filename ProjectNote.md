<h1>Notes</h1>
<ol>
    <li>Why <code>restTemplate.exchange()</code>?</li>
    - To send <code>auth token</code> and <code>body parameters</code>
    <li>Why <code>jdbcTemplate.executeQuery()</code> with <code>preparedStatement</code>?</li>
    - To execute complex SQL query with subQueries and union operator.<br>
    - To minimize multiple DB queries with complex SQL query
    <li>Why use same <code>model</code> for multiple <code>API responses</code> with different set of elements that's returned by server?</li>
    - To minimize the project footprint. All the unset/unmet fields/variables in model after mapping to a response either return <code>null</code> or <code>0</code> depending upon the data types used (<code>int</code> returns <code>0</code> by default and <code>String/Compound data type</code> returns <code>null</code>)
    <li>Why <code>Lombok</code> plugin?</li>
    - To minimize the project footprint and speed up the development process
    <li>Why <code>asciitable</code> plugin?</li>
    - To present the data in tabular form in the console without needing to write too much code
</ol>
<ul>
    <li><code>restTemplate</code>'s HTTP method (request) <em>must</em> match with <code>API-Controller</code>'s HTTP response method (response)</li>
    <li>To accept a <code>List&lt;of_objects&gt;</code> as <code>restTemplate</code>'s response (<code>responseEntity</code>) in the client code (services) add <code>new ParameterizedTypeReference<>() {}</code> as <code>returned class type</code></li>
    Example:<br>
    <code><em>ResponseEntity&lt;List&lt;TransfersHistory&gt;&gt; res =  restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
</em></code>
    <li>Working with <code>BigDecimal</code> data type is little tricky</li>
    <blockquote>
        <pre>BigDecimal a = new BigDecimal("5.9");<br>BigDecimal b = new BigDecimal("4.6");</pre>

    System.out.println(a.subtract(b));
    System.out.println(a.signum()>0);
    System.out.println(a.subtract(b).signum()<0);

    System.out.println(Math.signum(5));
    System.out.println(Math.signum(4.5));
    System.out.println(Math.signum(0));
    System.out.println(Math.signum(0.0));
    System.out.println(Math.signum(-4.5));
    System.out.println(Math.signum(-5));
</blockquote>
    
</ul>