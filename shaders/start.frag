#version 150

in vec2 texCoord;
in vec3 light;
in vec3 viewDirection;
in vec3 normal;

uniform sampler2D texture1;

out vec4 outColor;// output from the fragment shader

void main() {
    vec4 ambient = vec4(vec3(0.2), 1.0);

    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.7), 1.0);

    vec3 halfD = normalize(light + viewDirection);
    float NdotH = dot(normalize(normal), halfD);
    vec4 specular = vec4(pow(NdotH, 16) * vec3(1), 1);

    // TODO specular

    vec4 finalColor = ambient + diffuse + specular;
    vec4 textureColor = texture(texture1, texCoord);
    vec4 color = vec4(0.0,1.0,0.0,1.0);

    outColor = finalColor * color; //textureColor;

    // outColor = vec4(1.0, gl_FragCoord.y / 600f, 0.0, 1.0);
}
