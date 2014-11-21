//
//  ViewController.h
//  GrocerList
//
//  Created by Esau Rubio on 11/4/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController
{
    IBOutlet UITextField *username;
    IBOutlet UITextField *password;
    IBOutlet UIButton *loginButton;
}

-(IBAction)onClick:(id)sender;

@end

