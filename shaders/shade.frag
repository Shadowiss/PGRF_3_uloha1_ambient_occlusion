#version 420
in vec2 texCoord;
uniform int lightMode;
out vec4 outColor;

layout (binding=0) uniform sampler2D positionTexture;
layout (binding=1) uniform sampler2D normalTexture;
layout (binding=2) uniform sampler2D depthTexture;
layout (binding=3) uniform sampler2D ssaoTexture;
layout (binding=4) uniform sampler2D imageTexture;
uniform mat4 view;

//uniform vec3 light;
vec3 lightPosition = vec3(10, 10, 10);

void main() {
    vec3 position = texture(positionTexture, texCoord).xyz;
    vec3 normal = normalize(texture(normalTexture, texCoord).xyz);
    float AO = texture(ssaoTexture, texCoord).r;
    vec3 viewDirection = vec3(2) -(view.xyz * position);

    vec4 ambient = vec4(vec3(0.8) * AO, 1.0);

    vec3 light = normalize(lightPosition - position);
    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.5), 1.0);

    vec3 halfD = normalize(light + viewDirection);
    float NdotH = dot(normalize(normal), halfD);
    vec4 specular = vec4(pow(NdotH, 50) * vec3(1), 1);


    vec4 finalColor;// = ambient + diffuse + specular;

    //    vec4 textureColor = texture(texture1, texCoord);
    //    outColor = finalColor * textureColor;
    vec4 color = vec4(0.0,1.0,0.0,1.0);
    //outColor = finalColor;// * color;
    if(lightMode == 0){
            finalColor = ambient + diffuse + specular;
            outColor = finalColor * color;
    }
    else if(lightMode == 1){
        finalColor = ambient; //+ diffuse + specular;
        outColor = finalColor;// * color;
    }
    else if(lightMode == 2){
        finalColor = ambient+ diffuse; // + specular;
        outColor = finalColor;// * color;
    }
    else if(lightMode == 3){
        finalColor = ambient + diffuse + specular;
        outColor = finalColor;// * color;
    }
}
