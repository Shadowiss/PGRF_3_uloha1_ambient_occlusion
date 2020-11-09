#version 150

in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

uniform int temp;
uniform vec3 lightPos;
uniform float time;

out vec2 texCoord;
out vec3 light;
out vec3 viewDirection;
out vec3 normal;
out vec4 objPos;

const float PI = 3.1415;

vec3 getSphere(vec2 pos) {
    float az = pos.x * PI;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI;PI>
    float ze = pos.y * PI / 2;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI/2;PI/2>
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze);

    return vec3(x, y, z);
}

//vec3 getNormalSphere(vec2 pos) {
//    float az = pos.x * PI;
//    float ze = pos.y * PI / 2;
//
//    vec3 u = vec3(-PI * sin(az) * cos(ze), PI * cos(az) * cos(ze), 0);
//    vec3 v = vec3(-PI/2 * cos(az) * sin(ze), -PI/2 * sin(az) * sin(ze), PI/2 * cos(ze));
//
//    return cross(u, v);
//}

vec3 getNormalSphere2(vec2 pos) {
    vec3 u = getSphere(pos + vec2(0.001, 0)) - getSphere(pos - vec2(0.001, 0));
    vec3 v = getSphere(pos + vec2(0, 0.001)) - getSphere(pos - vec2(0, 0.001));
    return cross(u, v);
}
vec3 functionCM(vec2 pos)
{
    //My
    float t = pos.x* 2.0*PI;
    float s = pos.y * 2.0*PI;

    float x = cos(t);
    float y = sin(t)+cos(s);
    float z = sin(s);
    return vec3(x, y, z);
}
vec3 functionC(vec2 pos){
    //Torus
    float t = pos.x* 2.0*PI;
    float s = pos.y * 2.0*PI;
    float x = (3*cos(s)+cos(t)*cos(s))/3;
    float y = (3*sin(s)+cos(t)*sin(s))/3;
    float z = sin(t)/3;
    return vec3(x, y, z);
}
vec3 functionS(vec2 pos)
{
    float t = PI*pos.x;
    float s = 2*PI*pos.y;
    float r = 1;
    float phi = t;
    float theta = s;

    float x = r * sin(phi) * cos(theta);
    float y = r * sin(phi) * sin(theta);
    float z = r* cos(phi);
    return vec3(x, y, z);
}
vec3 functionS2(vec2 pos)
{
    float t = PI*pos.x;
    float s = 2*PI*pos.y;
    float r = 3+cos(4*s);
    float phi = t;
    float theta = s;

    float x = r * sin(phi) * cos(theta);
    float y = r * sin(phi) * sin(theta);
    float z = r* cos(phi);
    return vec3(x, y, z);
}
vec3 functionCyl(vec2 pos)
{
    float s = 2*PI * pos.x;
    float t = 2*PI * pos.y;
    float theta = s;
    float r = t;


    float x = r * cos(theta);
    float y = r * sin(theta);
    float z = 2*sin(t);
    return vec3(x, y, z);


}
vec3 functionCyl2(vec2 pos)
{
    float s = 2*PI * pos.x;
    float t = 2*PI* pos.y;

    float theta = s;
    float r = 1 + cos(t);


    float x = r * cos(theta);
    float y = r * sin(theta);
    float z = 2 - t;
    return vec3(x, y, z);

}



//float getZ(vec2 pos) {
//    return sin(pos.x * 10);
//}

void main() {
    texCoord = inPosition;
    //    vec2 position = vec2(inPosition.x * 2 - 1, inPosition.y * 2 - 1);
    vec2 position = inPosition * 2 - 1;
    vec3 pos3;

//    vec3 normal;
    if (temp == 1) {
        //pos3 = getSphere(position);
        //normal = getNormalSphere2(position);
        normal = getSphere(position);
    } else if (temp == 2) {
        // jiné těleso
        //pos3 = functionC(position);
        normal = functionC(position);

    }
    switch(temp){
        case 1:  normal = getSphere(position);
                 break;
        case 2:  normal = functionC(position);
                 break;
        case 3:  normal = functionCyl(position);
        break;
        case 4:  normal = functionCyl2(position);
        break;
        case 5:  normal = functionS2(position);
        break;
        case 6:  normal = functionCM(position);
        break;

    }

    gl_Position = projection * view * vec4(normal * time, 1.0);

    vec4 pos4 = vec4(normal, 1.0);
    light = lightPos - (view * pos4).xyz;
//    light = lightPos - (mat3(view) * pos3);

    viewDirection = -(view * pos4).xyz;
}
