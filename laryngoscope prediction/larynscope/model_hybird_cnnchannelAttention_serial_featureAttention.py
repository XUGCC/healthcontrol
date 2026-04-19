import os
os.environ['HF_ENDPOINT']='https://hf-mirror.com'

import timm
import torch
from torch import nn, optim
from einops import rearrange, repeat
v=timm.create_model('vit_base_r50_s16_224',pretrained=True,num_classes=6)
class FFN(nn.Module):
    def __init__(self, hidden_dim, drop_out=0.0):
        super().__init__()
        self.FFN = nn.Sequential(
            nn.Linear(768, hidden_dim),
            nn.GELU(),
            nn.Dropout(drop_out),
            nn.Linear(hidden_dim, hidden_dim),
            nn.GELU(),
            nn.Dropout(drop_out),
            nn.Linear(hidden_dim, 768),
            nn.Dropout(drop_out)
        )
    def forward(self, x):
        x=x+self.FFN(x)
        return x

class FeatureAttention(nn.Module):
    def __init__(self,dim=1, heads = 1, dim_head = 1, dropout = 0.):
        super().__init__()
        inner_dim=dim_head *  heads
        self.heads = heads
        self.to_qkv = nn.Linear(dim, inner_dim* 3, bias=False)

        self.scale = dim_head ** -0.5
        self.attend = nn.Softmax(dim = -1)
        self.dropout = nn.Dropout(dropout)
        self.to_out = nn.Sequential(
            nn.Linear(inner_dim, dim),
            nn.Dropout(dropout)
        )
    def forward(self,x):
        x=rearrange(x,'a b c-> a c b')

        unsqueeze = torch.unsqueeze(x, -1)

        qkv = self.to_qkv(unsqueeze).chunk(3, dim=-1)
        q, k, v = map(lambda t: rearrange(t, 'b f n (h d) -> b f h n d', h=self.heads), qkv)
        dots = torch.matmul(q, k.transpose(-1, -2)) * self.scale

        attn = self.attend(dots)
        attn = self.dropout(attn)

        out = torch.matmul(attn, v)
        out = rearrange(out, 'b f h n d -> b f n (h d)')

        out=self.to_out(out)
        out=rearrange(out,'a b c d-> a c b d')
        return torch.squeeze(out,dim=-1)

class ChannelAttention(nn.Module):
    def __init__(self, in_planes, drop_out=0.,hidden_dim=2048):
        super(ChannelAttention, self).__init__()
        self.proj = nn.Linear(768, 1024)
        self.conv=nn.Sequential(
            nn.Conv2d(in_channels=in_planes,out_channels=196,kernel_size=7,stride=2),
            nn.Conv2d(in_channels=196, out_channels=in_planes, kernel_size=7, stride=2),
                            )
        self.proj2=nn.Sequential(
            nn.Linear(16,1)
        )
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        x=self.proj(x)
        x=rearrange(x,'a b (c d)->a b c d',c=32)
        out = self.conv(x)
        out = rearrange(out, 'a b c d->a b (c d)', )
        out=self.proj2(out)
        channel_weights=self.sigmoid(out)

        return channel_weights


class ChannelFeatureAttention(nn.Module):
    def __init__(self,  drop_out=0., hidden_dim=2048):
        super(ChannelFeatureAttention, self).__init__()
        self.featureAttention=FeatureAttention()
        self.channelAttention=ChannelAttention(in_planes=196)
        self.Norm=nn.LayerNorm(768)
        self.FFN = FFN(hidden_dim,drop_out)

    def forward(self, x):
        identity=x

        weight=identity+self.featureAttention(x)
        weight = self.Norm(weight)

        ffn_out = weight + self.FFN(weight)
        featureAttention_out = self.Norm(ffn_out)

        channel_Attention=featureAttention_out*self.channelAttention(featureAttention_out)
        pre_out=self.Norm(channel_Attention)

        ffn_out = pre_out + self.FFN(pre_out)
        ffn_out = self.Norm(ffn_out)

        return ffn_out

class My_net(nn.Module):
    def __init__(self):
        super(My_net, self).__init__()
        self.patch_embed=v.patch_embed
        self.channel1 = ChannelFeatureAttention(drop_out=0.0)
        self.channel2 = ChannelFeatureAttention(drop_out=0.0)
        self.ViT=v.blocks
        self.mlp_head = nn.Sequential(
            nn.LayerNorm(768),
            nn.Linear(768, 6)
        )
    def forward(self,x):
        x1=self.patch_embed(x)
        x1 = self.channel1(x1)
        x1 = self.channel2(x1)

        x2=self.ViT(x1)
        x2=x2.mean(dim=1)
        output=self.mlp_head(x2)
        return output

if __name__ == '__main__':
    model=My_net()
    model(torch.ones(1,3,224,224))