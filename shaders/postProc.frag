#version 150

in vec2 texCoord;

uniform sampler2D texture1;

out vec4 outColor; // output from the fragment shader

void main() {
    //outColor = vec4(texCoord, 0, 1);
    outColor = texture(texture1, texCoord);
//    outColor = vec4(1.0, gl_FragCoord.y / 600f, 0.0, 1.0);
}
