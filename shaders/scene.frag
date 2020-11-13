#version 420

in vec4 positionVS;// VS = view space
in vec3 normalMS;// MS = model space
in vec3 normalVS;// VS = view space
in vec2 texCoord;

layout (binding=0) uniform sampler2D texture1;

layout (location=0) out vec4 outColor0;
layout (location=1) out vec4 outColor1;
layout (location=2) out vec4 outColor2;
layout (location=3) out vec4 outColor3;

void main() {
    outColor0 = vec4(positionVS.xyz, 1);
    outColor1 = vec4(normalize(normalMS), 1);
    outColor2 = vec4(normalize(normalVS), 1);
    outColor3 = texture(texture1, texCoord);
}