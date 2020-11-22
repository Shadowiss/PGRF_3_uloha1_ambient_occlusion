#version 420
in vec2 texCoord;
uniform int lightMode;
out vec4 outColor;
const float PI = 3.1415;

layout (binding=0) uniform sampler2D positionTexture;
layout (binding=1) uniform sampler2D normalTexture;
layout (binding=2) uniform sampler2D depthTexture;
layout (binding=3) uniform sampler2D ssaoTexture;
layout (binding=4) uniform sampler2D imageTexture;

uniform sampler2D texture1;
uniform mat4 view;
uniform mat4 rotZ;
vec3 lightPosition;
vec4 finalColor;

void main() {
    //Preset
    lightPosition = vec3(1, 1, 1)  * vec3(rotZ);
    vec3 position = texture(positionTexture, texCoord).xyz;
    vec3 normal = normalize(texture(normalTexture, texCoord).xyz);
    float AO = texture(ssaoTexture, texCoord).r;
    vec3 viewDirection = vec3(2) -(view.xyz * position);

    //Ambient
    vec4 ambient = vec4(vec3(0.8) * AO, 1.0);
    //Diffuse
    vec3 light = normalize(lightPosition - position);
    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.5), 1.0);
    //Specular
    vec3 halfD = normalize(light + viewDirection);
    float NdotH = max(0.0, dot(normalize(normal), halfD));
    vec4 specular = vec4(pow(NdotH, 8) * vec3(1), 1);

    //vec4 textureColor = texture(texture1, texCoord);
    vec4 color = vec4(0.0, 1.0, 0.0, 1.0);

//Chosing which color model will be applied
    switch(lightMode){
        //Blinn-phong + color
        case 0 :
            finalColor = ambient + diffuse + specular;
            outColor = finalColor * color;
            break;
        // Ambient
        case 1 :
            finalColor = ambient;
            outColor = finalColor;
            break;
        // Ambient + diffuse
        case 2 :
            finalColor = ambient+ diffuse;
            outColor = finalColor;
            break;
        // Abmient + diffuse + specular
        case 3 :
            finalColor = ambient + diffuse + specular;
            outColor = finalColor;
            break;
        // Reflector
        case 4 :
            float spotCutOff = 0.99;
            float dist = length(light);
            float att = 1.0/(1.0+0.002*dist+0.01*(dist*dist));

            float spotEffect = max(dot(normalize(vec3(lightPosition.xy * -0.01, -1)), normalize(-light)), 0);
            float blend = clamp((spotEffect-spotCutOff)/(1-spotCutOff), 0.0, 1.0);
            if (spotEffect > spotCutOff) {
                outColor = mix(ambient, ambient+att*(diffuse +specular), blend);
            } else
            {
                outColor=ambient;
            }
            break;
    }
}
