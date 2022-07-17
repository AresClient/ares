#version 330 core

in vec2 vertUV;
in vec4 vertColor;

uniform float radius = 0.08; // 0 to 1
uniform vec2 size = vec2(1, 1); // ratio w to h of the rect

out vec4 fragColor;

void main() {
    vec2 factor = size / max(size.x, size.y);
    float distance = length(max(abs(vertUV * factor) - 1.0 * factor + radius, 0.0)) - radius;
    float alpha = 1.0 - smoothstep(0.0, 1.5, distance / fwidth(distance));
    if(alpha == 0.0) discard;
    fragColor = vec4(vertColor.rgb, vertColor.a * alpha);
}
