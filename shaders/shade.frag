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
//uniform sampler2D texture1;
uniform mat4 view;
uniform mat3 rotY;
uniform mat4 rotZ;
uniform float time;
//uniform float spotCutOff;
//uniform vec3 spotDirection;
//uniform vec3 light;
vec3 lightPosition;

void main() {
    if(lightMode <4 && lightMode >-1){
        lightPosition = vec3(1,1,1)  * vec3(rotZ);
    }else{
        lightPosition = vec3(1, 1, 1) * vec3(rotZ); ;
    }

    vec3 position = texture(positionTexture, texCoord).xyz;
    vec3 normal = normalize(texture(normalTexture, texCoord).xyz);
    float AO = texture(ssaoTexture, texCoord).r;
    vec3 viewDirection = vec3(2) -(view.xyz * position);

    vec4 ambient = vec4(vec3(0.8) * AO, 1.0);

    vec3 light = normalize(lightPosition - position);
    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.5), 1.0);

    vec3 halfD = normalize(light + viewDirection);
    //float NdotH = dot(normalize(normal), halfD);
    float NdotH = max(0.0,dot(normalize(normal), halfD));
    vec4 specular = vec4(pow(NdotH, 8) * vec3(1), 1);



    vec4 finalColor;// = ambient + diffuse + specular;

    //vec4 textureColor = texture(texture1, texCoord);
    vec4 color = vec4(0.0,1.0,0.0,1.0);
    //outColor = finalColor;// * color;

    if(lightMode == 0){
        finalColor = ambient + diffuse + specular;
        outColor = finalColor * color; // * textureColor;
    }
    else if(lightMode == 1){
        finalColor = ambient;
        outColor = finalColor;
    }
    else if(lightMode == 2){
        finalColor = ambient+ diffuse;
        outColor = finalColor;
    }
    else if(lightMode == 3){
        finalColor = ambient + diffuse + specular;
        outColor = finalColor;
    }
    else if(lightMode == 4){
        float spotCutOff = 0.1;
        //vec3 spotDirection = normalize((vec3(1)*vec3(rotZ)) - position);
        vec3 spotDirection = -vec3(1, 1, 1); // * vec3(rotZ);

        float dist=length(light);
        //float spotCutOff = 0.7;

        float att = 1.0/(1.0+0.02*dist+0.01*(dist*dist));
        //float att = 1.0/(0.7 * dist);
        float spotEffect = max(dot(normalize(spotDirection),normalize(-light)),0);
        float blend = clamp((spotEffect-spotCutOff)/(1-spotCutOff),0.0,1.0);

        if (spotEffect > spotCutOff) {
            //outColor=(ambient) + att*((diffuse) + (specular));
            outColor = mix(ambient,ambient+att*(diffuse +specular),blend);

        }else
        {
            outColor=ambient;
        }
    }
}
