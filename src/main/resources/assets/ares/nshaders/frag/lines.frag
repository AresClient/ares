#version 330 core

in vec4 vertColor;
noperspective in vec4 vertDist; // uv, width/2, length/2

uniform float AARadius = 2.0;

out vec4 fragColor;

void main() {
    float au = 1.0 - smoothstep(1.0 - ((2.0 * AARadius) / vertDist.z), 1.0, abs(vertDist.x / vertDist.z));
    float av = 1.0 - smoothstep(1.0 - ((2.0 * AARadius) / vertDist.w), 1.0, abs(vertDist.y / vertDist.w));
    float alpha = min(au, av);
    if(alpha == 0.0) discard;
    fragColor = vec4(vertColor.rgb, vertColor.a * alpha);
}
