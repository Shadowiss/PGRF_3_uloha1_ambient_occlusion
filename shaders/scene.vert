#version 150

in vec2 inPosition;// input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;
uniform int temp;
uniform float time;

out vec3 normalMS;// MS = model space
out vec3 normalVS;// VS = view space
out vec4 positionVS;// VS = view space
out vec2 texCoord;
//out vec3 viewDirection;

const float PI = 3.1415;

vec3 getSphere(vec2 pos) {
    float az = pos.x * PI;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI;PI>
    float ze = pos.y * PI / 2;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI/2;PI/2>
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze) ;

    return vec3(x/2, y/2, z/2);
}

vec3 getSphereNormal(vec2 pos) {
    vec3 u = getSphere(pos + vec2(0.001, 0)) - getSphere(pos - vec2(0.001, 0));
    vec3 v = getSphere(pos + vec2(0, 0.001)) - getSphere(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getPlane(vec2 pos) {
    return vec3(-1, pos.x, pos.y);
}

vec3 getPlaneNormal(vec2 pos) {
    vec3 u = getPlane(pos + vec2(0.001, 0)) - getPlane(pos - vec2(0.001, 0));
    vec3 v = getPlane(pos + vec2(0, 0.001)) - getPlane(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getPlane2(vec2 pos) {
    return vec3(pos.x, pos.y, -1);
}

vec3 getPlane2Normal(vec2 pos) {
    vec3 u = getPlane2(pos + vec2(0.001, 0)) - getPlane2(pos - vec2(0.001, 0));
    vec3 v = getPlane2(pos + vec2(0, 0.001)) - getPlane2(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getFunctionCM(vec2 pos)
{
    float t = pos.x* 2.0*PI;
    float s = pos.y * 2.0*PI;

    float x = cos(t);
    float y = sin(t)+cos(s);
    float z = sin(s);
    return vec3(x/3, y/3, z/3);
}
vec3 getNormalFunctionCM(vec2 pos){
    vec3 u = getFunctionCM(pos + vec2(0.001, 0)) - getFunctionCM(pos - vec2(0.001, 0));
    vec3 v = getFunctionCM(pos + vec2(0, 0.001)) - getFunctionCM(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getFunctionC(vec2 pos){
    float t = pos.x* 2.0*PI;
    float s = pos.y * 2.0*PI;
    float x = (3*cos(s)+cos(t)*cos(s))/3;
    float y = (3*sin(s)+cos(t)*sin(s))/3;
    float z = sin(t)/3;
    return vec3(x/2, y/2, z/2);
}
vec3 getNormalFunctionC(vec2 pos){
    vec3 u = getFunctionC(pos + vec2(0.001, 0)) - getFunctionC(pos - vec2(0.001, 0));
    vec3 v = getFunctionC(pos + vec2(0, 0.001)) - getFunctionC(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getFunctionS(vec2 pos)
{
    float t = PI*pos.x;
    float s = 2*PI*pos.y;
    float r = 1;
    float phi = t;
    float theta = s;

    float x = r * sin(phi) * cos(theta);
    float y = r * sin(phi) * sin(theta);
    float z = r* cos(phi);
    return vec3(x/10, y/10, z/10);
}
vec3 getNormalFunctionS(vec2 pos){
    vec3 u = getFunctionS(pos + vec2(0.001, 0)) - getFunctionS(pos - vec2(0.001, 0));
    vec3 v = getFunctionS(pos + vec2(0, 0.001)) - getFunctionS(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getFunctionS2(vec2 pos)
{
    float t = PI*pos.x;
    float s = 2*PI*pos.y;
    float r = 3+cos(4*s);
    float phi = t;
    float theta = s;

    float x = r * sin(phi) * cos(theta);
    float y = r * sin(phi) * sin(theta);
    float z = r* cos(phi);
    return vec3(x/10, y/10, z/10);
}
vec3 getNormalFunctionS2(vec2 pos){
    vec3 u = getFunctionS2(pos + vec2(0.001, 0)) - getFunctionS2(pos - vec2(0.001, 0));
    vec3 v = getFunctionS2(pos + vec2(0, 0.001)) - getFunctionS2(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getFunctionCyl(vec2 pos)
{
    float s = 2*PI * pos.x;
    float t = 2*PI * pos.y;
    float theta = s;
    float r = t;

    float x = r * cos(theta);
    float y = r * sin(theta);
    float z = 2*sin(t);
    return vec3(x/10, y/10, z/10);
}
vec3 getNormalFunctionCyl(vec2 pos){
    vec3 u = getFunctionCyl(pos + vec2(0.001, 0)) - getFunctionCyl(pos - vec2(0.001, 0));
    vec3 v = getFunctionCyl(pos + vec2(0, 0.001)) - getFunctionCyl(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getFunctionCyl2(vec2 pos)
{
    float s = 2*PI * pos.x;
    float t = 2*PI* pos.y;

    float theta = s;
    float r = 1 + cos(t);

    float x = r * cos(theta);
    float y = r * sin(theta);
    float z = 2 - t;
    return vec3(x/10, y/10, z/10);
}
vec3 getNormalFunctionCyl2(vec2 pos){
    vec3 u = getFunctionCyl2(pos + vec2(0.001, 0)) - getFunctionCyl2(pos - vec2(0.001, 0));
    vec3 v = getFunctionCyl2(pos + vec2(0, 0.001)) - getFunctionCyl2(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getTruncatedCone(vec2 pos){
    float u = (pos.x + 1) ;// grid<-1;1>, - > <0;3>
    float v = (pos.y + 1) * PI;// gridu <-1;1> - > <0;2pi>
    float r = 0.5;
    float x = (2-((2 * u)/3)) * cos(v);
    float y = (2-((2 * u)/3)) * sin(v);
    float z = u;
    return vec3(x/5, y/5, z/5);
}
vec3 getNormalTruncatedCone(vec2 pos){
    vec3 u = getTruncatedCone(pos + vec2(0.001, 0)) - getTruncatedCone(pos - vec2(0.001, 0));
    vec3 v = getTruncatedCone(pos + vec2(0, 0.001)) - getTruncatedCone(pos - vec2(0, 0.001));
    return cross(u, v);
}

void main() {
    texCoord = inPosition;
    vec2 position = inPosition * 2 - 1;
    vec3 pos3;
    vec3 normal;

//    if (temp == 1) {
//        pos3 = getSphere(position);
//        normal = getSphereNormal(position);
//    } else if (temp == 2) {
//        pos3 = getPlane(position);
//        normal = getPlaneNormal(position);
//    } else if (temp == 3) {
//        pos3 = getPlane2(position);
//        normal = getPlane2Normal(position);
//    }

    switch(temp){
        case 1:  pos3 = getSphere(position);
                 normal = getSphereNormal(position);
        break;
        case 2:  pos3 = getFunctionC(position);
                 normal = getNormalFunctionC(position);
        break;
        case 3:  pos3 = getTruncatedCone(position);
                 normal = getNormalTruncatedCone(position);
        break;
        case 4:  pos3 = getFunctionCyl(position);
                 normal = getNormalFunctionCyl(position);
        break;
        case 5:  pos3 = getFunctionCyl2(position);
                 normal = getNormalFunctionCyl2(position);
        break;
        case 6:  pos3 = getFunctionS2(position);
                 normal = getNormalFunctionS2(position);
        break;
        case 7:  pos3 = getFunctionCM(position);
                 normal = getNormalFunctionCM(position);
        break;
        case 8:
        pos3 = getPlane(position);
        normal = getPlaneNormal(position);
        break;
        case 9:
        pos3 = getPlane2(position);
        normal = getPlane2Normal(position);
        break;

    }

    vec4 pos4 = vec4(pos3 * time , 1.0);
    gl_Position = projection * view * pos4;

//    vec4 pos4 = vec4(pos3, 1.0);
//    gl_Position = projection * view * pos4;

    positionVS = view * pos4;
    normalMS = normal;
    normalVS = transpose(inverse(mat3(view))) * normalMS;

}
