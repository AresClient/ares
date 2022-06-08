#version 330 core

in vec2 vertUV;

uniform sampler2D theTexture;
uniform vec4 dimensions;
uniform vec3 color;

out vec4 fragColor;

void main() {
    fragColor = vec4(color, texture(theTexture, vec2(vertUV.x * dimensions.z + dimensions.x, vertUV.y * dimensions.w + dimensions.y)).a);
}
